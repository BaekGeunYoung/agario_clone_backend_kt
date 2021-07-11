package com.example.kotlin_ws_agario.message.body

import com.example.kotlin_ws_agario.model.Position

sealed class IncomingMessageBody

data class EatBody(val preyId: String) : IncomingMessageBody()
data class MergeBody(val colonyId: String) : IncomingMessageBody()
data class PositionChangeBody(val position: Position) : IncomingMessageBody()
