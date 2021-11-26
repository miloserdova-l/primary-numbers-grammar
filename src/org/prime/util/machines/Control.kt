package org.prime.util.machines


class Control(input: List<String>) {
    private var tape: ArrayDeque<String> = ArrayDeque(input)
    private var currentPosition = 0



    fun move(direction: Direction) {
        when (direction) {
            Direction.LEFT -> moveLeft()
            Direction.RIGHT -> moveRight()
        }
    }

    fun readSymbol() = tape[currentPosition]

    fun writeSymbol(symbol: String) {
        tape[currentPosition] = symbol
    }

    private fun moveLeft() {
        currentPosition--
        if (currentPosition < 0) {
            tape.addFirst("_")
            currentPosition = 0
        }
    }

    private fun moveRight() {
        currentPosition++
        if (currentPosition >= tape.size) {
            tape.addLast("_")
        }
    }

}