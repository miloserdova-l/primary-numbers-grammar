package org.prime.util.machines

enum class Direction {
    LEFT,
    RIGHT;

    companion object {
        fun fromString(s: String): Direction? =
            when(s) {
                ">" -> RIGHT
                "<" -> LEFT
                else -> null
            }
    }
}