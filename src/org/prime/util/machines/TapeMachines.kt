package org.prime.util.machines

class TuringMachine : TapeMachine() {
    override fun prepareInputForTape(input: String) = input.toCharArray().map { it.toString() }
}

class LinearBoundedAutomaton: TapeMachine() {
    // In this case we need to surround input with border markers
    override fun prepareInputForTape(input: String): List<String> {
        return input.toCharArray().map { it.toString() }
    }
}

