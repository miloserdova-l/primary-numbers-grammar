import org.prime.util.runApp

fun main(args: Array<String>) = try {
    runApp(args)
} catch (e: AssertionError) {
    print(e.message)
}
