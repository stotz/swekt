package ch.typedef.swekt.examples

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Debug tool to inspect SE1 file structure.
 */
fun main(args: Array<String>) {
    val ephePath = System.getenv("SE_EPHE_PATH")
    if (ephePath == null) {
        println("SE_EPHE_PATH not set")
        return
    }

    val file = if (args.isNotEmpty()) {
        Paths.get(args[0])
    } else {
        Paths.get(ephePath, "sepl_18.se1")
    }

    if (!Files.exists(file)) {
        println("File not found: $file")
        return
    }

    println("Inspecting SE1 file: $file")
    println("File size: ${Files.size(file)} bytes")
    println()

    val bytes = Files.readAllBytes(file)
    val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

    println("First 100 bytes (hex):")
    for (i in 0 until minOf(100, bytes.size)) {
        print("%02X ".format(bytes[i]))
        if ((i + 1) % 16 == 0) println()
    }
    println()
    println()

    // Try to read first record
    println("Attempting to read first record:")
    try {
        val startJD = buffer.double
        val endJD = buffer.double
        val numCoeffs = buffer.int

        println("  Start JD: $startJD")
        println("  End JD: $endJD")
        println("  Time span: ${endJD - startJD} days")
        println("  Num coefficients: $numCoeffs")
        println()

        if (numCoeffs > 0 && numCoeffs < 100 && buffer.remaining() >= numCoeffs * 3 * 8) {
            println("  First 5 longitude coefficients:")
            for (i in 0 until minOf(5, numCoeffs)) {
                println("    [$i] = ${buffer.double}")
            }
        } else {
            println("  WARNING: Invalid coefficient count or insufficient data")
        }

    } catch (e: Exception) {
        println("  ERROR: ${e.message}")
        e.printStackTrace()
    }
}
