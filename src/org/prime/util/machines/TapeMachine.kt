package org.prime.util.machines

enum class TapeMachineType {
    TuringMachine,
    LinearBoundedAutomaton
}

abstract class TapeMachine {
    companion object {
        fun fromFile(path: String, type: TapeMachineType): TapeMachine {
            val reader = getMachineReader(type)
            return reader.readMachineFromFile(path)
        }
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

    val usedDeltas: ArrayList<Delta> = ArrayList()

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
        val convertedInput = prepareInputForTape(input)
        val control = Control(convertedInput)
        while (currentState != acceptState) {
            var foundDelta = false
            deltaTransitions.forEach { delta ->
                if (delta.currentState == currentState
                        && delta.readSymbol == control.readSymbol()) {
                    //println(delta)
                    usedDeltas.add(delta)
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

    abstract fun prepareInputForTape(input: String): List<String>
}

data class Delta(val currentState: String,
                 val readSymbol: String,
                 val nextState: String,
                 val writeSymbol: String,
                 val direction: Direction
)