package org.prime.util

import java.util.*

class Grammar (
    private val terminals: List<String>,
    private val nonTerminals: List<String>,
    private val startTerminal: String,
    val productions: List<Production>
        ) {
    override fun toString(): String {
        return """
Terminals: ${terminals.toStringSet()};
NonTerminals: ${nonTerminals.toStringSet()};
Start Terminal: $startTerminal;
Productions: ${productions.toStringSetWithEndLine()}
""".trimIndent()
    }

    private fun List<String>.toStringSet() = toString()
        .replace("[", "{\n")
        .replace("]", "\n}")

    private fun List<Production>.toStringSetWithEndLine() = toString()
        .replace("[", "{\n")
        .replace("]", "\n}")
        .replace(", ", "\n")
}


data class Production(val leftSymbols: List<String>, val rightSymbols: List<String>) {

    override fun toString(): String = "${leftSymbols.toPrettyString()} -> ${rightSymbols.toPrettyString()}"

    private fun List<String>.toStringNoBrackets() = toString().replace("[", "")
        .replace("]", "")

    private fun List<String>.toPrettyString() = toStringNoBrackets().replace(", ", " ")

    fun apply(word: List<String>): List<String>? {
        val newWord = word.toMutableList()
        val match = Collections.indexOfSubList(newWord, leftSymbols)
        if (match == -1) {
            return null
        }
        newWord.subList(match, match + leftSymbols.size).clear()
        newWord.addAll(match, rightSymbols.filter { it != Constants.EPSILON })
        return newWord
    }
}