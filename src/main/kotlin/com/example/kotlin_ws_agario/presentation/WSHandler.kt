package com.example.kotlin_ws_agario.presentation

import com.example.kotlin_ws_agario.actor.Connected
import com.example.kotlin_ws_agario.actor.UserIncomingMessage
import com.example.kotlin_ws_agario.actor.routeActor
import com.example.kotlin_ws_agario.actor.userActor
import com.example.kotlin_ws_agario.model.Rooms
import com.example.kotlin_ws_agario.presentation.message.WSIncomingMessage
import com.example.kotlin_ws_agario.presentation.message.WSOutgoingMessage
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.mono
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.core.publisher.Sinks

@InternalCoroutinesApi
class WSHandler: WebSocketHandler {
    private val jsonMapper = jsonMapper()

    override fun handle(session: WebSocketSession): Mono<Void> =
        mono {
            handleSuspended(session)
        }
            .then()

    private suspend fun handleSuspended(session: WebSocketSession) {
        val params = parseQueryString(session.handshakeInfo.uri)
        val userId = params["id"]!!

        val roomActor = Rooms.findOrCreate()
        val userActor = userActor(roomActor)

        val routeActor = routeActor(session)

        val connectedMsg = Connected(
            routeActor = routeActor,
            userId = userId,
            username = params["username"] ?: "anonymous"
        )

        userActor.send(connectedMsg)

        session.receive()
            .log()
            .map { it.retain() }
            .asFlow()
            .collect {
                val incomingMessage = jsonMapper.readValue(it.payloadAsText, WSIncomingMessage::class.java)
                val userIncomingMessage = UserIncomingMessage(
                    userId,
                    incomingMessage.toMessageBody()
                )

                userActor.send(userIncomingMessage)
                userIncomingMessage.completeStatus.await()
            }
    }
}


interface EventUnicastService {
    /**
     * Add message to stream
     * @param next - message which will be added to stream
     */
    fun onNext(next: WSOutgoingMessage)
    val messages: Flux<WSOutgoingMessage>
}

class EventUnicastServiceImpl: EventUnicastService {
    private val processor = Sinks.many().multicast().onBackpressureBuffer<WSOutgoingMessage>()
    override val messages
        get() = processor.asFlux()

    override fun onNext(next: WSOutgoingMessage) {
        processor.emitNext(next) { a, _ ->
            when (a) {
                SignalType.ON_ERROR -> true
                else -> false
            }
        }
    }
}