package org.prime.util

import org.prime.util.machines.LinearBoundedAutomaton
import org.prime.util.machines.TuringMachine

interface DerivationBuilder {
    fun buildDerivation(input: List<String>, grammar: Grammar): MutableList<String>
}

/**
 * Derivation builder uses TuringMachine to get accumulated used deltas to build derivations
 */
class DerivationBuilderT0(private val turingMachine: TuringMachine) : DerivationBuilder {
    override fun buildDerivation(input: List<String>, grammar: Grammar): MutableList<String> {
        var currentWord = mutableListOf(turingMachine.startState)
        val productions = grammar.productions
        input.forEach {
            currentWord.add(getBracketSymbol(it, it))
        }
        repeat(input.size) {
            currentWord.add(getBracketSymbol(Constants.EPSILON, "_"))
        }
        repeat(input.size + 2) {
            currentWord.add(0, getBracketSymbol(Constants.EPSILON, "_"))
        }
        val result = mutableListOf(currentWord.toPrettyWord())
        for (d in turingMachine.usedDeltas) {
            for (p in productions.filter { p ->
                p.leftSymbols.contains(d.currentState) && p.rightSymbols.contains(d.nextState)
            }) {
                val prodResult = p.apply(currentWord)?.toMutableList()
                if (prodResult != null) {
                    result.add(prodResult.toPrettyWord())
                    currentWord = prodResult
                    break
                }
            }
        }
        val finishProductions = productions.filter { p ->
            p.leftSymbols.contains(turingMachine.acceptState) && p.rightSymbols.size != 1
        }

        val acceptEps = productions.find {
            it.leftSymbols.contains(turingMachine.acceptState) && it.rightSymbols.size == 1
        }!!

        while (true) {
            var changed = false
            finishProductions.forEach {
                val newWord = it.apply(currentWord)
                if (newWord != null) {
                    result.add(newWord.toPrettyWord())
                    currentWord = newWord.toMutableList()
                    changed = true
                }
            }
            if (!changed) break
        }

        while (true) {
            var changed = false
            val newWord = acceptEps.apply(currentWord)
            if (newWord != null) {
                result.add(newWord.toPrettyWord())
                currentWord = newWord.toMutableList()
                changed = true
            }
            if (!changed) break
        }
        return result
    }

    private fun getBracketSymbol(left: String, right: String) = "($left|$right)"

    private fun List<String>.toPrettyWord() = toString().replace("[", "").replace("]", "")
}


class DerivationBuilderT1(val lba: LinearBoundedAutomaton) : DerivationBuilder {
    override fun buildDerivation(input: List<String>, grammar: Grammar): MutableList<String> {
        var currentWord = mutableListOf(lba.startState)
        val productions = grammar.productions
        input.forEach {
            currentWord.add(getBracketSymbol(it, it))
        }
        repeat(input.size) {
            currentWord.add(getBracketSymbol(Constants.EPSILON, "_"))
        }
        repeat(input.size + 2) {
            currentWord.add(0, getBracketSymbol(Constants.EPSILON, "_"))
        }
        val result = mutableListOf(currentWord.toPrettyWord())
        for (d in lba.usedDeltas) {
            for (p in productions.filter { p ->
                p.leftSymbols.contains(d.currentState) && p.rightSymbols.contains(d.nextState)
            }) {
                val prodResult = p.apply(currentWord)?.toMutableList()
                if (prodResult != null) {
                    result.add(prodResult.toPrettyWord())
                    currentWord = prodResult
                    break
                }
            }
        }
        val finishProductions = productions.filter { p ->
            p.leftSymbols.contains(lba.acceptState) && p.rightSymbols.size != 1
        }

        val cont: (String) -> Boolean = { it.contains(lba.acceptState) }
        val acceptEps = productions.find {
            it.leftSymbols.any(cont) && it.rightSymbols.size == 1
        }!!

        while (true) {
            var changed = false
            finishProductions.forEach {
                val newWord = it.apply(currentWord)
                if (newWord != null) {
                    result.add(newWord.toPrettyWord())
                    currentWord = newWord.toMutableList()
                    changed = true
                }
            }
            if (!changed) break
        }

        while (true) {
            var changed = false
            val newWord = acceptEps.apply(currentWord)
            if (newWord != null) {
                result.add(newWord.toPrettyWord())
                currentWord = newWord.toMutableList()
                changed = true
            }
            if (!changed) break
        }
        return result
    }

    private fun getBracketSymbol(left: String, right: String) = "($left|$right)"

    private fun List<String>.toPrettyWord() = toString().replace("[", "").replace("]", "")

}

