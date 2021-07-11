package com.example.kotlin_ws_agario.presentation.message

import com.example.kotlin_ws_agario.message.body.EatBody
import com.example.kotlin_ws_agario.message.body.IncomingMessageBody
import com.example.kotlin_ws_agario.message.body.MergeBody
import com.example.kotlin_ws_agario.message.body.PositionChangeBody
import com.example.kotlin_ws_agario.message.type.IncomingMessageType
import com.example.kotlin_ws_agario.presentation.jsonMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class WSIncomingMessage(
    val type: IncomingMessageType,
    val body: JsonNode
) {
    companion object {
        private val jsonMapper = jsonMapper()
    }

    fun toMessageBody(): IncomingMessageBody = when (type) {
        IncomingMessageType.POSITION_CHANGED -> jsonMapper.treeToValue(body, PositionChangeBody::class.java)
        IncomingMessageType.MERGE -> jsonMapper.treeToValue(body, MergeBody::class.java)
        IncomingMessageType.EAT -> jsonMapper.treeToValue(body, EatBody::class.java)
    }
}
