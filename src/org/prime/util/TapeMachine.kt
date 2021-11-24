package org.prime.util

import java.io.File

class TapeMachine {
    companion object {
        fun fromFile(path: String) = MachineReader.readMachineFromFile(path)
    }

    val states: ArrayList<String> = ArrayList()

    var startState: String = ""
        set(value) {
            if (states.contains(value)) {
                field = value
            }
        }

    var acceptState: String = ""
        set(value) {
            if (states.contains(value)) {
                field = value
            }
        }

    val deltaTransitions: ArrayList<Delta> = ArrayList()

    var gamma: List<String> = ArrayList()

    var sigma: List<String> = ArrayList()


    fun addState(state: String): Boolean {
        if (states.contains(state)) {
            return false
        }
        states.add(state)
        return true
    }

    fun addDelta(delta: Delta): Boolean {
        if (deltaTransitions.contains(delta)) {
            return false
        }
        deltaTransitions.add(delta)
        return true
    }

    private var currentState: String = ""

    fun accept(input: String): Boolean {
        currentState = startState
        val convertedInput = input.toCharArray().map { it.toString() }
        val control = Control(convertedInput)
        while (currentState != acceptState) {
            var foundDelta = false
            deltaTransitions.forEach { delta ->
                if (delta.currentState == currentState
                        && delta.readSymbol == control.readSymbol()) {
                    //println(delta)
                    currentState = delta.nextState
                    control.writeSymbol(delta.writeSymbol)
                    control.move(delta.direction)
                    foundDelta = true
                    return@forEach
                }
            }
            if (!foundDelta) {
                return false
            }
        }
        return true
    }

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
}

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

data class Delta(val currentState: String,
                 val readSymbol: String,
                 val nextState: String,
                 val writeSymbol: String,
                 val direction: Direction)

class MachineReader {
    companion object {
        fun readMachineFromFile(path: String): TapeMachine {
            val machine = TapeMachine()
            val file = File(path)
            var lines = file.readLines().filter {
                it.isNotEmpty() && !it.startsWith(Constants.COMMENT)
            }
            val initStr = lines[0].drop(6)
            val acceptStr = lines[1].drop(8)
            val sigmaStrings = lines[2].drop(7).let {
                it.substring(1, it.length - 1).split("${Constants.COMMA_DELIM} ")
            }
            val gamma = lines[3].drop(7).let {
                it.substring(1, it.length - 1).split("${Constants.COMMA_DELIM} ")
            }
            lines = lines.drop(4)
            val deltaLists = lines.partition {
                lines.indexOf(it) % 2 == 0
            }
            val deltas = deltaLists.first.zip(deltaLists.second)

            listOf(
                deltaLists.first.map { it.split(Constants.COMMA_DELIM)[0] },
                deltaLists.second.map { it.split(Constants.COMMA_DELIM)[0] }
            ).flatten().distinct().toHashSet().forEach { state ->
                machine.addState(state)
            }
            machine.startState = initStr
            machine.acceptState = acceptStr
            machine.sigma = sigmaStrings
            machine.gamma = gamma

            deltas.forEach { delta ->
                val (splitRead, splitWrite) = delta.first.split(Constants.COMMA_DELIM) to
                        delta.second.split(Constants.COMMA_DELIM)
                val (q, readSymbol) = splitRead[0] to splitRead[1]
                val (p, writeSymbol, directionStr) = Triple(splitWrite[0], splitWrite[1], splitWrite[2])
                machine.addDelta(
                    Delta(q, readSymbol, p, writeSymbol, Direction.fromString(directionStr)!!)
                )
            }
            return machine
        }
    }

}