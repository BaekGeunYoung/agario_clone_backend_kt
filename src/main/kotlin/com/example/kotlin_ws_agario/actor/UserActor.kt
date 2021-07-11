package com.example.kotlin_ws_agario.actor

import com.example.kotlin_ws_agario.message.body.IncomingMessageBody
import com.example.kotlin_ws_agario.message.body.OutgoingMessageBody
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

sealed class UserActorMsg {
    val completeStatus = CompletableDeferred<Unit>()
}

class Connected(val routeActor: SendChannel<UserOutgoingMessage>, val userId: String, val username: String): UserActorMsg()
class UserIncomingMessage(val userId: String, val body: IncomingMessageBody): UserActorMsg()
class UserOutgoingMessage(val body: OutgoingMessageBody): UserActorMsg()

fun userActor(roomActor: SendChannel<RoomActorMsg>) = GlobalScope.actor<UserActorMsg> {
    lateinit var routeActor: SendChannel<UserOutgoingMessage>
    val roomActor = roomActor

    for (msg in channel) {
        when (msg) {
            is Connected -> {
                roomActor.send(Join(msg.userId, msg.username, this.channel))
                routeActor = msg.routeActor
            }
            is UserIncomingMessage -> {
                roomActor.send(IncomingMessage(msg.userId, msg.body))
            }
            is UserOutgoingMessage -> {
                routeActor.send(msg)
            }
        }
    }
}
