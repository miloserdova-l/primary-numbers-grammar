package org.prime.util.machines

import org.prime.util.Constants
import java.io.File

interface MachineReader {
    fun readMachineFromFile(path: String): TapeMachine
}

class TuringMachineReader : MachineReader {
    override fun readMachineFromFile(path: String): TapeMachine {
        val machine = TuringMachine()
        val file = File(path)
        return fullFillMachine(file, machine)
    }
}

class LinearBoundedAutomatonReader : MachineReader {
    override fun readMachineFromFile(path: String): TapeMachine {
        val machine = LinearBoundedAutomaton()
        val file = File(path)
        return fullFillMachine(file, machine)
    }
}

fun getMachineReader(type: TapeMachineType) =
    when (type) {
        TapeMachineType.TuringMachine -> TuringMachineReader()
        TapeMachineType.LinearBoundedAutomaton -> LinearBoundedAutomatonReader()
    }



private fun fullFillMachine(file: File, machine: TapeMachine): TapeMachine {
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