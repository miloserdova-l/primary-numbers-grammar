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
            gammaStrings.forEach { C ->
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
        return Grammar(sigmaStrings, "A1", productions.distinct())
    }

    private fun getBracketSymbol(left: String, right: String) = "($left|$right)"

}

class ConverterT1 : Converter {
    override fun machineToGrammar(machine: TapeMachine): Grammar {
        val sigmaStrings = machine.sigma
        val initStr = machine.startState
        val gammaStrings = machine.gamma
        val acceptStr = machine.acceptState
        val productions = ArrayList<Production>()


        for (sigma in sigmaStrings) {
            productions.add(Production(listOf("A1"), listOf("($initStr|_|$sigma|$sigma|_)")))
            productions.add(Production(listOf("A1"), listOf("($initStr|_|$sigma|$sigma)", "A2")))
            productions.add(Production(listOf("A2"), listOf("($sigma|$sigma)", "A2")))
            productions.add(Production(listOf("A2"), listOf("($sigma|$sigma|_)")))
        }


        for (delta in machine.deltaTransitions) {
            for (sigma in sigmaStrings) {
                if (delta.direction == Direction.RIGHT) {
                    if (delta.readSymbol == "_") {
                        for (e in gammaStrings) {

                            productions.add(
                                Production(
                                    listOf("(" + delta.currentState + "|_|" + e + "|" + sigma + "|_)"),
                                    listOf("(_|" + delta.nextState + "|" + e + "|" + sigma + "|_)")
                                )
                            )
                        }
                    } else {

                        productions.add(
                            Production(
                                listOf("(_|" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + "|_)"),
                                listOf("(_|" + delta.writeSymbol + "|" + sigma + "|" + delta.nextState + "|_)")
                            )
                        )
                    }
                } else {
                    if (delta.readSymbol == "_") {
                        for (gamma in gammaStrings) {
                            productions.add(
                                Production(
                                    listOf("(_|" + gamma + "|" + sigma + "|" + delta.currentState + "|_)"),
                                    listOf("(_|" + delta.nextState + "|" + gamma + "|" + sigma + "|_)")
                                )
                            )
                        }
                    } else {
                        productions.add(
                            Production(
                                listOf("(_|" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + "|_)"),
                                listOf("(" + delta.nextState + "|_|" + delta.writeSymbol + "|" + sigma + "|_)")
                            )
                        )
                    }
                }
            }
        }


        for (sigma in sigmaStrings) {
            for (gamma in gammaStrings) {
                productions.add(Production(listOf("($acceptStr|_|$gamma|$sigma|_)"), listOf(sigma)))
                productions.add(Production(listOf("(_|$acceptStr|$gamma|$sigma|_)"), listOf(sigma)))
                productions.add(Production(listOf("(_|$gamma|$sigma|$acceptStr|_)"), listOf(sigma)))
            }
        }



        for (delta in machine.deltaTransitions) {
            for (sigma in sigmaStrings) {
                if (delta.direction == Direction.RIGHT) {
                    if (delta.readSymbol == "_") {
                        for (gamma in gammaStrings) {
                            productions.add(
                                Production(
                                    listOf("(" + delta.currentState + "|_|" + gamma + "|" + sigma + ")"),
                                    listOf("(_|" + delta.nextState + "|" + gamma + "|" + sigma + ")")
                                )
                            )
                        }
                    } else {
                        for (gamma in gammaStrings) {
                            for (s1 in sigmaStrings) {
                                productions.add(
                                    Production(
                                        listOf("(_|" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + ")", "($gamma|$s1)"),
                                        listOf("(_|" + delta.writeSymbol + "|" + sigma + ")", "(" + delta.nextState + "|" + gamma + "|" + s1 + ")")
                                    )
                                )
                                productions.add(
                                    Production(
                                        listOf("(_|" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + ")", "($gamma|$s1|_)"),
                                        listOf("(_|" + delta.writeSymbol + "|" + sigma + ")", "(" + delta.nextState + "|" + gamma + "|" + s1 + "_)")
                                    )
                                )
                            }
                        }
                    }
                } else {
                    productions.add(
                        Production(
                            listOf("(_|" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + ")"),
                            listOf("(" + delta.nextState + "|_|" + delta.writeSymbol + "|" + sigma + ")")
                        )
                    )
                }
            }
        }


        for (delta in machine.deltaTransitions) {
            for (s1 in sigmaStrings) {
                for (s2 in sigmaStrings) {
                    if (delta.direction == Direction.RIGHT) {
                        for (gamma in gammaStrings) {
                            productions.add(
                                Production(
                                    listOf("(" + delta.currentState + "|" + delta.readSymbol + "|" + s1 + ")", "($gamma|$s2)"),
                                    listOf("(" + delta.writeSymbol + "|" + s1 + ")", "(" + delta.nextState + "|" + gamma + "|" + s2 + ")")
                                )
                            )
                            productions.add(
                                Production(
                                    listOf("(" + delta.currentState + "|" + delta.readSymbol + "|" + s1 + ")", "($gamma|$s2|_)"),
                                    listOf("(" + delta.writeSymbol + "|" + s1 + ")", "(" + delta.nextState + "|" + gamma + "|" + s2 + "|_)")
                                )
                            )
                        }
                    } else {
                        for (gamma in gammaStrings) {
                            productions.add(
                                Production(
                                    listOf("($gamma|$s2)", "(" + delta.currentState + "|" + delta.readSymbol + "|" + s1 + ")"),
                                    listOf("(" + delta.nextState + "|" + gamma + "|" + s2 + ")", "(" + delta.writeSymbol + "|" + s1 + ")")
                                )
                            )
                            productions.add(
                                Production(
                                    listOf("(_|$gamma|$s2)", "(" + delta.currentState + "|" + delta.readSymbol + "|" + s1 + ")"),
                                    listOf("(_|" + delta.nextState + "|" + gamma + "|" + s2 + ")", "(" + delta.writeSymbol + "|" + s1 + ")")
                                )
                            )
                        }
                    }
                }
            }
        }


        for (delta in machine.deltaTransitions) {
            for (sigma in sigmaStrings) {
                if (delta.direction == Direction.RIGHT) {
                    productions.add(
                        Production(
                            listOf("(" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + "|_)"),
                            listOf("(" + delta.writeSymbol + "|" + sigma + "|" + delta.nextState + "|_)")
                        )
                    )
                } else {
                    if (delta.readSymbol == "_") {
                        for (gamma in gammaStrings) {
                            productions.add(
                                Production(
                                    listOf("(" + gamma + "|" + sigma + "|" + delta.currentState + "|_)"),
                                    listOf("(" + delta.nextState + "|" + gamma + "|" + sigma + "|_)")
                                )
                            )
                        }
                    } else {
                        for (gamma in gammaStrings) {
                            for (s1 in sigmaStrings) {
                                productions.add(
                                    Production(
                                        listOf("(" + gamma + "|" + s1 + ")",  "(" + delta.currentState + "|" + delta.readSymbol + "|" + sigma + "|_)"),
                                        listOf("(" + delta.nextState + "|" + gamma + "|" + s1 + "_)", "(" + delta.writeSymbol + "|" + sigma + "|_)")
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }


        for (sigma in sigmaStrings) {
            for (gamma in gammaStrings) {
                productions.add(
                    Production(listOf("(" + acceptStr + "|_|" + gamma + "|" + sigma + ")"), listOf(sigma))
                )
                productions.add(
                    Production(listOf("(_|" + acceptStr + "|" + gamma + "|" + sigma + ")"), listOf(sigma))
                )
                productions.add(
                    Production(listOf("(" + acceptStr + "|" + gamma + "|" + sigma + ")"), listOf(sigma))
                )
                productions.add(
                    Production(listOf("(" + acceptStr + "|" + gamma + "|" + sigma + "|_)"), listOf(sigma))
                )
                productions.add(
                    Production(listOf("(" + gamma + "|" + sigma + "|" + acceptStr + "|_)"), listOf(sigma))
                )
            }
        }


        for (s1 in sigmaStrings) {
            for (s2 in sigmaStrings) {
                for (gamma in gammaStrings) {
                    productions.add(
                        Production(listOf(s1, "(" + gamma + "|" + s2 + ")"), listOf(s1, s2))
                    )
                    productions.add(
                        Production(listOf(s1, "(" + gamma + "|" + s2 + "|_)"), listOf(s1, s2))
                    )
                    productions.add(
                        Production(listOf("(" + gamma + "|" + s1 + ")", s2), listOf(s1, s2))
                    )
                    productions.add(
                        Production(listOf("(_|" + gamma + "|" + s1 + ")", s2), listOf(s1, s2))
                    )
                }
            }
        }
        return Grammar(sigmaStrings,"A1", productions.distinct())
    }
}