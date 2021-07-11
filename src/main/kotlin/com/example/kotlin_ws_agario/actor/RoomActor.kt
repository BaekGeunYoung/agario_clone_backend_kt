package com.example.kotlin_ws_agario.actor

import com.example.kotlin_ws_agario.message.body.EatBody
import com.example.kotlin_ws_agario.message.body.EatedBody
import com.example.kotlin_ws_agario.model.User
import com.example.kotlin_ws_agario.message.body.IncomingMessageBody
import com.example.kotlin_ws_agario.message.body.JoinBody
import com.example.kotlin_ws_agario.message.body.MergeBody
import com.example.kotlin_ws_agario.message.body.MergedBody
import com.example.kotlin_ws_agario.message.body.ObjectsBody
import com.example.kotlin_ws_agario.message.body.OutgoingMessageBody
import com.example.kotlin_ws_agario.message.body.PositionChangeBody
import com.example.kotlin_ws_agario.message.body.SeedBody
import com.example.kotlin_ws_agario.message.body.WasMergedBody
import com.example.kotlin_ws_agario.model.Position
import com.example.kotlin_ws_agario.model.Prey
import com.example.kotlin_ws_agario.model.Rooms.rooms
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import org.slf4j.LoggerFactory
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

sealed class RoomActorMsg

data class Join(val userId: String, val username: String, val channel: SendChannel<UserActorMsg>): RoomActorMsg()
data class IncomingMessage(val userId: String, val body: IncomingMessageBody): RoomActorMsg()

@ObsoleteCoroutinesApi
fun roomActor() = GlobalScope.actor<RoomActorMsg> {
    val log = LoggerFactory.getLogger("roomActorLogger")
    val initialRadius = 100.0
    val roomWidth = 30000.0
    val roomHeight = 30000.0
    val preyMaxNumber = 1000
    val preyRadius = 30.0

    fun genRandomPosition(): Position {
        val x = Random.nextDouble(initialRadius, roomWidth - initialRadius)
        val y = Random.nextDouble(initialRadius, roomHeight - initialRadius)
        return Position(x, y)
    }

    fun genRandomColor(): String {
        val charSet = "0123456789abcdef"

        var color = "#"

        (0 until 6).forEach {
            color += charSet[Random.nextInt(0, 16)]
        }

        return color
    }

    fun supplyPreys(num: Int): ConcurrentHashMap<String, Prey> =
        ConcurrentHashMap<String, Prey>().apply {
            (0 until num).map {
                val id = UUID.randomUUID().toString()
                val prey = Prey(id, genRandomPosition(), preyRadius, genRandomColor())
                this.put(id, prey)
            }
        }

    fun initPreys(): ConcurrentHashMap<String, Prey> = supplyPreys(preyMaxNumber)

    val users = ConcurrentHashMap<String, Pair<User, SendChannel<UserActorMsg>>>()
    val preys = initPreys()

    suspend fun broadCast(messageBody: OutgoingMessageBody) = users.values.forEach {
        it.second.send(UserOutgoingMessage(messageBody))
    }

    for (msg in channel) {
        when (msg) {
            is Join -> {
                val (userId, username) = msg
                val newUser = User(userId, username, genRandomPosition(), initialRadius, genRandomColor())

                users[userId] = newUser to msg.channel
                log.info("new user joined. current user list: ${users.map { it.key }}")

                rooms[rooms.indexOfFirst { it.first == this.channel }] = this.channel to users.size

                broadCast(JoinBody(newUser))
                log.info("sent JOIN. username: ${newUser.username}")
            }
            is IncomingMessage -> {
                val (userId, message) = msg
                when (message) {
                    is PositionChangeBody -> {
                        val user = users[userId]

                        if (user == null) {
                            log.info("specified user is null")
                        }

                        if (user != null) {
                            user.first.position = message.position
                            log.info("changed position of ${user.first.username} into ${message.position}")
                            broadCast(ObjectsBody(users.map { it.value.first }, preys.values.toList()))
                        }
                    }
                    is MergeBody -> {
                        val colonyId = message.colonyId

                        log.info("received MERGE. colonyId: $colonyId, conquererId: $userId")

                        val conquererElement = users[userId]
                        val colonyElement = users[colonyId]

                        if (conquererElement != null && colonyElement != null) {
                            val (conquerer, _) = conquererElement
                            val (colony, colonyActor) = colonyElement

                            val distance = conquerer.position.distanceFrom(colony.position)
                            val canMerge = distance <= conquerer.radius

                            if (canMerge) {
                                conquerer.updateRadius(colony.radius)

                                users.remove(colonyId)

                                // merged message를 모두에게 보냄
                                broadCast(MergedBody(conquerer, colonyId))
                                log.info("sent MERGED, conquerer: ${conquerer.username}, colony: ${colony.username}")

                                // merge 당한 유저에게는 wasMerged message를 보냄
                                colonyActor.send(UserOutgoingMessage(WasMergedBody))
                                log.info("sent WAS_MERGED, conquerer: ${conquerer.username}, colony: ${colony.username}")
                            }
                        }
                    }
                    is EatBody -> {
                        val preyId = message.preyId
                        log.info("received EAT, user: $userId, preyId: $preyId")

                        val eaterElement = users[userId]
                        val prey = preys[preyId]

                        if (eaterElement != null && prey != null) {
                            val (eater, _) = eaterElement
                            val distance = eater.position.distanceFrom(prey.position)
                            val canEat = distance <= eater.radius

                            // eat 가능한지 validation
                            if (canEat) {
                                eater.updateRadius(prey.radius)

                                preys -= preyId

                                // eated message를 모두에게 보냄
                                broadCast(EatedBody(eater, preyId))
                                log.info("sent EATED, eater: ${eater.username}, preyId: $preyId")

                                // 먹이 갯수가 많이 떨어지면 seeding 해주기
                                if (preys.size < preyMaxNumber / 2) {
                                    val newPreys = supplyPreys(preyMaxNumber / 2)

                                    preys.putAll(newPreys)

                                    broadCast(SeedBody(newPreys.values.toList()))
                                    log.info("sent SEED")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}