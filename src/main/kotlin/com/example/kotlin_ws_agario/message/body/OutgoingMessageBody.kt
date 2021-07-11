package com.example.kotlin_ws_agario.message.body

import com.example.kotlin_ws_agario.model.Prey
import com.example.kotlin_ws_agario.model.User

sealed class OutgoingMessageBody

data class JoinBody(val newUser: User) : OutgoingMessageBody()
data class ObjectsBody(val users: List<User>, val preys: List<Prey>) : OutgoingMessageBody()
data class MergedBody(val userAfterMerge: User, val colonyId: String) : OutgoingMessageBody()
object WasMergedBody : OutgoingMessageBody()
data class SeedBody(val newPreys: List<Prey>) : OutgoingMessageBody()
data class EatedBody(val userAfterEat: User, val preyId: String) : OutgoingMessageBody()
