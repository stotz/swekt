package ch.typedef.swekt.tools

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Paths

/**
 * SE1 Binary Format Inspector
 * 
 * Analyzes Swiss Ephemeris SE1 files to understand the binary structure.
 */
fun se1Inspector(fileName: String = "sepl_18.se1") {
    val ephePath = System.getenv("SE_EPHE_PATH")
    if (ephePath == null) {
        println("ERROR: SE_EPHE_PATH not set")
        println("Usage: set SE_EPHE_PATH=/path/to/swisseph")
        return
    }

    val file = Paths.get(ephePath, fileName)

    if (!Files.exists(file)) {
        println("ERROR: File not found: $file")
        println("Available files in $ephePath:")
        Files.list(Paths.get(ephePath))
            .filter { it.fileName.toString().endsWith(".se1") }
            .forEach { println("  - ${it.fileName}") }
        return
    }

    println("=" .repeat(70))
    println("SE1 Binary Format Inspector")
    println("=" .repeat(70))
    println("File: $file")
    println("Size: ${Files.size(file)} bytes")
    println()

    val bytes = Files.readAllBytes(file)
    
    // Show first 256 bytes as hex dump
    println("First 256 bytes (HEX):")
    println("-" .repeat(70))
    for (i in 0 until minOf(256, bytes.size) step 16) {
        print("%04X: ".format(i))
        for (j in 0 until 16) {
            if (i + j < bytes.size) {
                print("%02X ".format(bytes[i + j]))
            } else {
                print("   ")
            }
        }
        print(" | ")
        for (j in 0 until 16) {
            if (i + j < bytes.size) {
                val c = bytes[i + j].toInt() and 0xFF
                print(if (c in 32..126) c.toChar() else '.')
            }
        }
        println()
    }
    println()

    // Try to parse as Swiss Ephemeris header (Little Endian)
    println("Attempting to parse as Swiss Ephemeris header (LITTLE_ENDIAN):")
    println("-" .repeat(70))
    val bufferLE = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
    
    try {
        val lndx0 = bufferLE.int
        val iflg = bufferLE.int
        val ncoe = bufferLE.int
        val rmaxInt = bufferLE.int
        
        val tfstart = bufferLE.double
        val tfend = bufferLE.double
        val dseg = bufferLE.double
        val telem = bufferLE.double
        val prot = bufferLE.double
        val dprot = bufferLE.double
        val qrot = bufferLE.double
        val dqrot = bufferLE.double
        val peri = bufferLE.double
        val dperi = bufferLE.double
        
        println("Offset  0 (lndx0):    $lndx0 (0x${"%08X".format(lndx0)}) - Index position")
        println("Offset  4 (iflg):     $iflg (0x${"%08X".format(iflg)}) - Flags")
        println("Offset  8 (ncoe):     $ncoe (0x${"%08X".format(ncoe)}) - Num coefficients")
        println("Offset 12 (rmax*1000): $rmaxInt (0x${"%08X".format(rmaxInt)}) - Normalization")
        println()
        println("Offset 16 (tfstart):  $tfstart JD - Start Julian Day")
        println("Offset 24 (tfend):    $tfend JD - End Julian Day")
        println("Offset 32 (dseg):     $dseg days - Segment size")
        println("Offset 40 (telem):    $telem JD - Epoch")
        println("Offset 48 (prot):     $prot")
        println("Offset 56 (dprot):    $dprot")
        println("Offset 64 (qrot):     $qrot")
        println("Offset 72 (dqrot):    $dqrot")
        println("Offset 80 (peri):     $peri")
        println("Offset 88 (dperi):    $dperi")
        println()
        
        // Validation
        println("VALIDATION:")
        println("  ncoe in range [1-100]: ${ncoe in 1..100}")
        println("  tfstart > 0: ${tfstart > 0}")
        println("  tfend > tfstart: ${tfend > tfstart}")
        println("  dseg reasonable: ${dseg in 1.0..10000.0}")
        
        if (ncoe in 1..100 && tfstart > 0 && tfend > tfstart) {
            println()
            println("✅ Header appears VALID!")
            println()
            
            // Calculate number of segments
            val numSegments = ((tfend - tfstart + 0.1) / dseg).toInt()
            println("Calculated segments: $numSegments")
            println()
            
            // Try to read index
            if (lndx0 > 0 && lndx0 < bytes.size) {
                println("Index position: $lndx0 (0x${"%08X".format(lndx0)})")
                bufferLE.position(lndx0)
                
                println()
                println("First 10 index entries:")
                for (i in 0 until minOf(10, numSegments)) {
                    if (bufferLE.remaining() >= 4) {
                        val pos = bufferLE.int
                        println("  [$i] Position: $pos (0x${"%08X".format(pos)})")
                    }
                }
            }
        } else {
            println()
            println("❌ Header validation FAILED - trying BIG_ENDIAN...")
            println()
            tryBigEndian(bytes)
        }
        
    } catch (e: Exception) {
        println("ERROR parsing: ${e.message}")
        e.printStackTrace()
    }
}

fun tryBigEndian(bytes: ByteArray) {
    println("Attempting to parse as Swiss Ephemeris header (BIG_ENDIAN):")
    println("-" .repeat(70))
    val bufferBE = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
    
    try {
        val lndx0 = bufferBE.int
        val iflg = bufferBE.int
        val ncoe = bufferBE.int
        val rmaxInt = bufferBE.int
        
        val tfstart = bufferBE.double
        val tfend = bufferBE.double
        val dseg = bufferBE.double
        
        println("Offset  0 (lndx0):    $lndx0 (0x${"%08X".format(lndx0)})")
        println("Offset  4 (iflg):     $iflg (0x${"%08X".format(iflg)})")
        println("Offset  8 (ncoe):     $ncoe (0x${"%08X".format(ncoe)})")
        println("Offset 12 (rmax*1000): $rmaxInt (0x${"%08X".format(rmaxInt)})")
        println()
        println("Offset 16 (tfstart):  $tfstart JD")
        println("Offset 24 (tfend):    $tfend JD")
        println("Offset 32 (dseg):     $dseg days")
        println()
        
        println("VALIDATION:")
        println("  ncoe in range [1-100]: ${ncoe in 1..100}")
        println("  tfstart > 0: ${tfstart > 0}")
        println("  tfend > tfstart: ${tfend > tfstart}")
        
        if (ncoe in 1..100 && tfstart > 0 && tfend > tfstart) {
            println()
            println("✅ Header appears VALID with BIG_ENDIAN!")
        } else {
            println()
            println("❌ Header validation FAILED with BIG_ENDIAN too")
        }
        
    } catch (e: Exception) {
        println("ERROR: ${e.message}")
    }
}
