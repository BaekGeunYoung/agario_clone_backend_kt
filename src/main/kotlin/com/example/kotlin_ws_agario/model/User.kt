package com.example.kotlin_ws_agario.model

import kotlin.math.sqrt

data class User(
    val id: String,
    val username: String,
    var position: Position,
    var radius: Double,
    val color: String
) {
    fun updateRadius(smallRadius: Double) {
        val areaSum = (Math.PI * radius * radius) + (Math.PI * smallRadius * smallRadius)
        this.radius = sqrt(areaSum / Math.PI)
    }
}
