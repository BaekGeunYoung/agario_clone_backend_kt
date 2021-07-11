package com.example.kotlin_ws_agario.model

import com.example.kotlin_ws_agario.actor.RoomActorMsg
import com.example.kotlin_ws_agario.actor.roomActor
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import java.lang.Exception

object Rooms {
    private const val roomCapacity = 100
    var rooms: MutableList<Pair<SendChannel<RoomActorMsg>, Int>> = mutableListOf()

    fun findOrCreate(): SendChannel<RoomActorMsg> {
        val affordables = rooms.filter { it.second < roomCapacity }

        return if (affordables.isEmpty()) createNewRoom()
        else affordables.first().first
    }

    private fun createNewRoom() = roomActor()
            .also { actor -> rooms.add(actor to 0) }

}
