package ch.typedef

import ch.typedef.swekt.tools.se1Inspector

/**
 * Main application class for swekt.
 */
class Swekt {
    fun run() {
        println("swekt - Swiss Ephemeris Kotlin Toolkit")
        println("Ready.")
    }
}

/**
 * Main entry point for swekt
 *
 * Supports various tools via command-line arguments.
 */
fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }

    when (args[0].lowercase()) {
        "se1inspector", "inspect" -> {
            val fileName = if (args.size > 1) args[1] else "sepl_18.se1"
            se1Inspector(fileName)
        }
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
        
        Usage:
          ./gradlew run --args="COMMAND [options]"
        
        Commands:
          se1inspector [file]   Inspect SE1 binary file format
                                (default: sepl_18.se1)
          help                  Show this help
        
        Examples:
          ./gradlew run --args="se1inspector"
          ./gradlew run --args="se1inspector sepl_00.se1"
          ./gradlew run --args="se1inspector semo_18.se1"
        
        Environment:
          SE_EPHE_PATH         Path to Swiss Ephemeris data files
    """.trimIndent())
}
