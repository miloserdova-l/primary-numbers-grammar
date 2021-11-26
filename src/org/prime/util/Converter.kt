package org.prime.util

import org.prime.util.machines.Direction
import org.prime.util.machines.TapeMachine

interface Converter {
    fun machineToGrammar(machine: TapeMachine): Grammar
}

object Constants {
    const val EPSILON = "epsilon"
    const val RIGHT = ">"
    const val COMMENT = "//"
    const val BLANK = "_"
    const val COMMA_DELIM = ","
}

class ConverterT0 : Converter {
    override fun machineToGrammar(machine: TapeMachine): Grammar {
        val sigmaStrings = machine.sigma
        val initStr = machine.startState
        val gammaStrings = machine.gamma
        val acceptStr = machine.acceptState

        val nonTerminals = ArrayList<String>()
        (sigmaStrings + listOf(Constants.EPSILON)).forEach { X ->
            gammaStrings.forEach { Y ->
                nonTerminals.add(getBracketSymbol(X, Y))
            }
        }
        nonTerminals.addAll(listOf("A1", "A2", "A3"))
        nonTerminals.addAll(machine.states)

        val productions = ArrayList<Production>()
        // following Martynenko: https://core.ac.uk/download/pdf/217165386.pdf
        // (1)
        productions.add(Production(listOf("A1"), listOf(initStr, "A2")))
        // (2)
        sigmaStrings.forEach {
            productions.add(Production(listOf("A2"), listOf(getBracketSymbol(it, it), "A2")))
        }
        // (3)
        productions.add(Production(listOf("A2"), listOf("A3")))
        // (4)
        productions.add(Production(listOf("A3"), listOf(getBracketSymbol(Constants.EPSILON, Constants.BLANK), "A3")))
        // (5)
        productions.add(Production(listOf("A3"), listOf(Constants.EPSILON)))

        val (deltasRight, deltasLeft) = machine.deltaTransitions.partition {
            it.direction == Direction.RIGHT
        }
        // (6)
        deltasRight.forEach { delta ->
            (sigmaStrings + listOf(Constants.EPSILON)).forEach {
                productions.add(Production(listOf(delta.currentState, getBracketSymbol(it, delta.readSymbol)),
                    listOf(getBracketSymbol(it, delta.writeSymbol), delta.nextState)))
            }
        }
        // (7)
        deltasLeft.forEach { delta ->
            (sigmaStrings + listOf(Constants.EPSILON)).forEach { a ->
                (sigmaStrings + listOf(Constants.EPSILON)).forEach { b ->
                    gammaStrings.forEach { E ->
                        productions.add(Production(listOf(getBracketSymbol(b, E), delta.currentState, getBracketSymbol(a, delta.readSymbol)),
                            listOf(delta.nextState, getBracketSymbol(b, E), getBracketSymbol(a, delta.writeSymbol))))
                    }
                }
            }
        }
        // (8)
        (sigmaStrings + listOf(Constants.EPSILON)).forEach { a ->
            sigmaStrings.forEach { C ->
                productions.add(
                    Production(listOf(getBracketSymbol(a, C), acceptStr), listOf(acceptStr, a, acceptStr))
                )
                productions.add(
                    Production(listOf(acceptStr, getBracketSymbol(a, C)), listOf(acceptStr, a, acceptStr))
                )
                productions.add(
                    Production(listOf(acceptStr), listOf(Constants.EPSILON))
                )
            }
        }
        return Grammar(sigmaStrings, nonTerminals, "A1", productions)
    }

    private fun getBracketSymbol(left: String, right: String) = "($left|$right)"

}

class ConverterT1 : Converter {
    override fun machineToGrammar(machine: TapeMachine): Grammar {
        TODO("Not yet implemented")
    }
}