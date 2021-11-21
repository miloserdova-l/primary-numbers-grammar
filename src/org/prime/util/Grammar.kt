package org.prime.util

class Grammar (
    private val terminals: List<String>,
    private val nonTerminals: List<String>,
    private val startTerminal: String,
    private val productions: List<Production>
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


class Production(private val leftSymbols: List<String>, private val rightSymbols: List<String>) {

    override fun toString(): String = "${leftSymbols.toPrettyString()} -> ${rightSymbols.toPrettyString()}"

    private fun List<String>.toStringNoBrackets() = toString().replace("[", "")
        .replace("]", "")

    private fun List<String>.toPrettyString() = toStringNoBrackets().replace(", ", " ")

}