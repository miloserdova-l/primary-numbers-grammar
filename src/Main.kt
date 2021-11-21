import org.prime.util.ConverterT0

fun main() {
    val converter = ConverterT0()
    val grammarStr = converter.getGrammarFromFileWithAutomaton("res/automatons/prime_tm.txt").toString()
}
