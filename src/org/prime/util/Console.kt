package org.prime.util

import org.prime.util.machines.TapeMachine
import org.prime.util.machines.TapeMachineType
import java.io.File

const val TURING_MACHINE_FILE_PATH = "../res/automatons/prime_tm.txt"
const val LBA_FILE_PATH = "../res/automatons/prime_lba.txt"
const val T0_GRAMMAR_FILE_PATH = "../res/grammars/grammar_t0.txt"
const val T1_GRAMMAR_FILE_PATH = "../res/grammars/grammar_t1.txt"
const val T0_DERIVATION_PATH = "../res/derivations/T0.txt"
const val T1_DERIVATION_PATH = "../res/derivations/T1.txt"

fun runApp(args: Array<String>) {
    if (args.size != 1) return
    val type = getType(args[0]) ?: return
    val machine = TapeMachine.fromFile(getMachineFilePath(type), type)
    val converter = UtilsFactory.getConverter(type)
    val grammar = converter.machineToGrammar(machine)
    val fileForGrammar = File(getGrammarPath(type))
    fileForGrammar.writeText(grammar.toString())
    while (true) {
        print(">> ")
        val input = readLine()!!
        if (input == "quit") {
            return
        }
        val intInput = input.toIntOrNull() ?: continue
        val strInput = "1".repeat(intInput)
        machine.accept(strInput).let {
            when(it) {
                true -> {
                    println("Yes, number is prime")
                    val derivationBuilder = UtilsFactory.getDerivationBuilder(type, machine)
                    val derivationFile = File(getDerivationPath(type))
                    val derivation = derivationBuilder.buildDerivation(strInput.toCharArray()
                        .map { c -> c.toString() }, grammar)
                        .joinToString(separator = "\n")
                    derivationFile.writeText(derivation)
                    println("Derivation is written in file")
                    machine.usedDeltas.clear()
                }
                false -> println("No, number is not prime")
            }
        }
    }
}


fun getType(arg: String) = when(arg) {
    "T0" -> TapeMachineType.TuringMachine
    "T1" -> TapeMachineType.LinearBoundedAutomaton
    else -> null
}

fun getMachineFilePath(type: TapeMachineType) = when(type) {
    TapeMachineType.TuringMachine -> TURING_MACHINE_FILE_PATH
    TapeMachineType.LinearBoundedAutomaton -> LBA_FILE_PATH
}

fun getGrammarPath(type: TapeMachineType) = when(type) {
    TapeMachineType.TuringMachine -> T0_GRAMMAR_FILE_PATH
    TapeMachineType.LinearBoundedAutomaton -> T1_GRAMMAR_FILE_PATH
}

fun getDerivationPath(type: TapeMachineType) = when(type) {
    TapeMachineType.TuringMachine -> T0_DERIVATION_PATH
    TapeMachineType.LinearBoundedAutomaton -> T1_DERIVATION_PATH
}
