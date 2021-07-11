package com.example.kotlin_ws_agario

import com.example.kotlin_ws_agario.actor.Join
import com.example.kotlin_ws_agario.actor.roomActor
import com.example.kotlin_ws_agario.actor.userActor
import com.example.kotlin_ws_agario.message.body.JoinBody
import com.example.kotlin_ws_agario.model.Position
import com.example.kotlin_ws_agario.model.Rooms
import com.example.kotlin_ws_agario.model.Rooms.rooms
import com.example.kotlin_ws_agario.model.User
import com.example.kotlin_ws_agario.presentation.jsonMapper
import com.example.kotlin_ws_agario.presentation.message.WSOutgoingMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import kotlin.coroutines.coroutineContext

class KotlinWsAgarioApplicationTests {

    @Test
    fun contextLoads() {
    }

    @Test
    fun t() {
        runBlocking {
            val roomActor = Rooms.findOrCreate()
            val userActor = userActor(roomActor)

            roomActor.send(
                Join(userId = "qwewqe", username = "asdasd", channel = userActor)
            )

            println()
        }
    }

    @Test
    fun test() {
        val mapper = jsonMapper()

        val joinBody = JoinBody(
            newUser = User(
                id = "123",
                username = "abc",
                position = Position(x = 1.0, y = 0.5),
                radius = 10.5,
                color = "123123"
            )
        )

        val wsOutgoingMessage = WSOutgoingMessage.fromMessageBody(joinBody)

        val result = mapper.writeValueAsString(wsOutgoingMessage)

        println(result)

        val regressed = mapper.readValue(result, WSOutgoingMessage::class.java)

        val result2 = mapper.treeToValue(regressed.body, JoinBody::class.java)

        println(result2)
    }
}
