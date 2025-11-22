package ch.typedef

/**
 * Main application class for swekt.
 */
class Swekt {
    fun run() {
        println("swekt - Swiss Ephemeris Kotlin Toolkit")
        println("Version: 0.0.7")
        println("Using JPL Ephemeris (DE440/DE441)")
        println("Ready.")
    }
}

/**
 * Main entry point for swekt
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        Swekt().run()
        return
    }

    when (args[0].lowercase()) {
        "help", "--help", "-h" -> printUsage()
        else -> {
            println("Unknown command: ${args[0]}")
            println()
            printUsage()
        }
    }
}

private fun printUsage() {
    println("""
        swekt - Swiss Ephemeris Kotlin Toolkit
        
        High-precision astronomical calculations using JPL Ephemeris.
        
        Usage:
          ./gradlew run
        
        For examples, see:
          src/main/kotlin/ch/typedef/swekt/examples/
        
        Environment:
          SE_EPHE_PATH         Path to JPL ephemeris data files
                               (containing de440.eph, de441.eph, etc.)
    """.trimIndent())
}
