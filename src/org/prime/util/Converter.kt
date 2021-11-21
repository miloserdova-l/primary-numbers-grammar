package org.prime.util

import java.io.File

interface Converter {
    fun getGrammarFromFileWithAutomaton(path: String) : Grammar
}

object Constants {
    const val EPSILON = "epsilon"
    const val RIGHT = ">"
    const val COMMENT = "//"
    const val BLANK = "_"
    const val COMMA_DELIM = ","
}



class ConverterT0 : Converter {
    override fun getGrammarFromFileWithAutomaton(path: String): Grammar {
        val file = File(path)
        var lines = file.readLines().filter {
            it.isNotEmpty() && !it.startsWith(Constants.COMMENT)
        }
        val initStr = lines[0].drop(6)
        val acceptStr = lines[1].drop(8)
        val sigmaStrings = lines[2].drop(7).let {
            it.substring(1, it.length - 1).split("${Constants.COMMA_DELIM} ")
        }
        val gammaStrings = lines[3].drop(7).let {
            it.substring(1, it.length - 1).split("${Constants.COMMA_DELIM} ")
        }
        lines = lines.drop(4)
        val deltaLists = lines.partition {
            lines.indexOf(it) % 2 == 0
        }
        val deltas = deltaLists.first.zip(deltaLists.second)

        val nonTerminals = ArrayList<String>()
        (sigmaStrings + listOf(Constants.EPSILON)).forEach { X ->
            gammaStrings.forEach { Y ->
                nonTerminals.add(getBracketSymbol(X, Y))
            }
        }
        nonTerminals.addAll(listOf("A1", "A2", "A3"))
        nonTerminals.addAll(
            listOf(
                deltaLists.first.map { it.split(",")[0] },
                deltaLists.second.map { it.split(",")[0] }
            ).flatten().distinct()
        )

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

        val (deltasLeft, deltasRight) = deltas.partition {
            it.second.split(Constants.COMMA_DELIM)[2] == Constants.RIGHT
        }
        // (6)
        deltasRight.forEach { delta ->
            val (q, readSymbol) = delta.first.split(Constants.COMMA_DELIM)[0] to
                    delta.first.split(Constants.COMMA_DELIM)[1]
            val (p, writeSymbol) = delta.second.split(Constants.COMMA_DELIM)[0] to
                    delta.first.split(Constants.COMMA_DELIM)[1]

            (sigmaStrings + listOf(Constants.EPSILON)).forEach {
                productions.add(Production(listOf(q, getBracketSymbol(it, readSymbol)),
                    listOf(getBracketSymbol(it, writeSymbol), p)))
            }
        }
        // (7)
        deltasLeft.forEach { delta ->
            val (q, readSymbol) = delta.first.split(Constants.COMMA_DELIM)[0] to
                    delta.first.split(Constants.COMMA_DELIM)[1]
            val (p, writeSymbol) = delta.second.split(Constants.COMMA_DELIM)[0] to
                    delta.first.split(Constants.COMMA_DELIM)[1]

            (sigmaStrings + listOf(Constants.EPSILON)).forEach { a ->
                (sigmaStrings + listOf(Constants.EPSILON)).forEach { b ->
                    gammaStrings.forEach { E ->
                        productions.add(Production(listOf(getBracketSymbol(b, E), q, getBracketSymbol(a, readSymbol)),
                            listOf(p, getBracketSymbol(b, E), getBracketSymbol(a, writeSymbol))))
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