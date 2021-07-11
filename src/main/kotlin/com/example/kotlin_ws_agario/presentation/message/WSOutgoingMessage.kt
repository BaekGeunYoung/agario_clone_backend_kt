package com.example.kotlin_ws_agario.presentation.message

import com.example.kotlin_ws_agario.message.body.EatedBody
import com.example.kotlin_ws_agario.message.body.JoinBody
import com.example.kotlin_ws_agario.message.body.MergedBody
import com.example.kotlin_ws_agario.message.body.ObjectsBody
import com.example.kotlin_ws_agario.message.body.OutgoingMessageBody
import com.example.kotlin_ws_agario.message.body.SeedBody
import com.example.kotlin_ws_agario.message.body.WasMergedBody
import com.example.kotlin_ws_agario.message.type.OutgoingMessageType
import com.example.kotlin_ws_agario.presentation.jsonMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

data class WSOutgoingMessage(
    val type: OutgoingMessageType,
    val body: JsonNode
) {
    companion object {
        private val jsonMapper = jsonMapper()

        fun fromMessageBody(messageBody: OutgoingMessageBody): WSOutgoingMessage =
            jsonMapper.valueToTree<JsonNode>(messageBody).let { body ->
                when (messageBody) {
                    is EatedBody -> WSOutgoingMessage(OutgoingMessageType.EATED, body)
                    is JoinBody -> WSOutgoingMessage(OutgoingMessageType.JOIN, body)
                    is MergedBody -> WSOutgoingMessage(OutgoingMessageType.MERGED, body)
                    is ObjectsBody -> WSOutgoingMessage(OutgoingMessageType.OBJECTS, body)
                    is SeedBody -> WSOutgoingMessage(OutgoingMessageType.SEED, body)
                    WasMergedBody -> WSOutgoingMessage(OutgoingMessageType.WAS_MERGED, body)
                }
            }
    }
}

