package ch.typedef.swekt.io

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Swiss Ephemeris file header.
 *
 * Contains metadata about the ephemeris file:
 * - File format and version
 * - Time range (start/end Julian Day)
 * - Physical constants (AU, EMB ratio, etc.)
 * - Byte order (endianness)
 *
 * @property magic Magic number (file signature)
 * @property fileFormat Detected file format
 * @property version File format version
 * @property startJD Start Julian Day
 * @property endJD End Julian Day
 * @property byteOrder Byte order (little/big endian)
 * @property numConstants Number of constants in file
 * @property astronomicalUnit Astronomical Unit in km
 * @property earthMoonRatio Earth-Moon mass ratio
 * @property headerSize Size of header in bytes
 */
data class EphemerisFileHeader(
    val magic: String,
    val fileFormat: FileFormat,
    val version: Int,
    val startJD: Double,
    val endJD: Double,
    val byteOrder: ByteOrder,
    val numConstants: Int,
    val astronomicalUnit: Double,
    val earthMoonRatio: Double,
    val headerSize: Int
) {
    
    companion object {
        private const val MIN_HEADER_SIZE = 256
        private const val MAGIC_SIZE = 4
        
        /**
         * Reads ephemeris file header.
         *
         * @param file Path to ephemeris file
         * @return Parsed header
         * @throws IllegalArgumentException if file is invalid
         */
        @JvmStatic
        fun read(file: Path): EphemerisFileHeader {
            require(Files.exists(file)) { "File does not exist: $file" }
            require(Files.isRegularFile(file)) { "Not a regular file: $file" }
            require(Files.size(file) >= MIN_HEADER_SIZE) { 
                "File too small to contain valid header: $file" 
            }
            
            val bytes = Files.readAllBytes(file)
            
            // Try to read with little endian first (most common)
            val result = tryReadHeader(bytes, ByteOrder.LITTLE_ENDIAN)
                ?: tryReadHeader(bytes, ByteOrder.BIG_ENDIAN)
            
            return result ?: throw IllegalArgumentException(
                "Invalid ephemeris file format: $file"
            )
        }
        
        /**
         * Attempts to read header with specific byte order.
         *
         * @param bytes File bytes
         * @param byteOrder Byte order to try
         * @return Header if successful, null otherwise
         */
        private fun tryReadHeader(bytes: ByteArray, byteOrder: ByteOrder): EphemerisFileHeader? {
            return try {
                val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
                
                // Read magic number (4 bytes)
                val magicBytes = ByteArray(MAGIC_SIZE)
                buffer.get(magicBytes)
                val magic = String(magicBytes)
                
                // Validate magic
                if (!isValidMagic(magic)) {
                    return null
                }
                
                // Determine file format
                val fileFormat = when {
                    magic.startsWith("SE") -> FileFormat.SE1
                    magic.startsWith("JPL") -> FileFormat.JPL
                    else -> FileFormat.UNKNOWN
                }
                
                // Read version
                val version = buffer.int
                
                // Read time range
                val startJD = buffer.double
                val endJD = buffer.double
                
                // Validate time range
                if (endJD <= startJD || startJD < 0) {
                    return null
                }
                
                // Read number of constants
                val numConstants = buffer.int
                if (numConstants < 0 || numConstants > 10000) {
                    return null
                }
                
                // Read astronomical unit
                val au = buffer.double
                if (au < 1.0e8 || au > 2.0e8) {
                    return null
                }
                
                // Read Earth-Moon ratio
                val embRatio = buffer.double
                if (embRatio < 50.0 || embRatio > 100.0) {
                    return null
                }
                
                // Read header size
                val headerSize = buffer.int
                if (headerSize < MIN_HEADER_SIZE || headerSize > bytes.size) {
                    return null
                }
                
                EphemerisFileHeader(
                    magic = magic,
                    fileFormat = fileFormat,
                    version = version,
                    startJD = startJD,
                    endJD = endJD,
                    byteOrder = byteOrder,
                    numConstants = numConstants,
                    astronomicalUnit = au,
                    earthMoonRatio = embRatio,
                    headerSize = headerSize
                )
            } catch (e: Exception) {
                null
            }
        }
        
        /**
         * Validates magic number.
         *
         * @param magic Magic string
         * @return True if valid
         */
        private fun isValidMagic(magic: String): Boolean {
            return magic.matches(Regex("^[A-Z]{4}$"))
        }
    }
}
