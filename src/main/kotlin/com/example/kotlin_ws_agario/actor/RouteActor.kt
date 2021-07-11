package com.example.kotlin_ws_agario.actor

import com.example.kotlin_ws_agario.presentation.jsonMapper
import com.example.kotlin_ws_agario.presentation.message.WSOutgoingMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.kotlin.core.publisher.toMono

fun routeActor(session: WebSocketSession) = GlobalScope.actor<UserOutgoingMessage> {
    val jsonMapper = jsonMapper()

    for (msg in channel) {
        val wsOutgoingMessage = WSOutgoingMessage.fromMessageBody(msg.body)

        session.send(
            session.textMessage(jsonMapper.writeValueAsString(wsOutgoingMessage)).toMono()
        ).awaitSingleOrNull()

        msg.completeStatus.complete(Unit)
    }
}
