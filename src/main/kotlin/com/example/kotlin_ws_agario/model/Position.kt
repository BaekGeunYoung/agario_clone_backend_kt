package com.example.kotlin_ws_agario.model

import kotlin.math.pow
import kotlin.math.sqrt

data class Position(
    var x: Double,
    var y: Double
) {
    fun distanceFrom(other: Position): Double =
    sqrt((x - other.x).pow(2) + (y - other.y).pow(2))
}
