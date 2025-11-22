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
         * For SE1 files: Reads Swiss Ephemeris binary format header
         * For JPL files: Reads JPL format header
         *
         * @param file Path to ephemeris file
         * @return Parsed header
         * @throws IllegalArgumentException if file is invalid
         */
        @JvmStatic
        fun read(file: Path): EphemerisFileHeader {
            require(Files.exists(file)) { "File does not exist: $file" }
            require(Files.isRegularFile(file)) { "Not a regular file: $file" }
            
            val bytes = Files.readAllBytes(file)
            
            // SE1 files have Swiss Ephemeris binary format
            if (file.fileName.toString().endsWith(".se1")) {
                return readSwissEphFormat(bytes, file)
            }
            
            // Try to read JPL or other formats
            val result = tryReadHeader(bytes, ByteOrder.LITTLE_ENDIAN)
                ?: tryReadHeader(bytes, ByteOrder.BIG_ENDIAN)
            
            return result ?: throw IllegalArgumentException(
                "Invalid ephemeris file format: $file"
            )
        }
        
        /**
         * Reads Swiss Ephemeris binary format header.
         *
         * SE1 Format (per planet):
         * - lndx0 (4 bytes int32): File position of planet's index
         * - iflg (4 bytes int32): Flags (helio/bary, rotation, ellipse)
         * - ncoe (4 bytes int): Number of Chebyshev coefficients
         * - rmax_int (4 bytes int32): Normalization factor * 1000
         * - tfstart (8 bytes double): Start Julian Day
         * - tfend (8 bytes double): End Julian Day
         * - dseg (8 bytes double): Segment size in days
         * - telem, prot, dprot, qrot, dqrot, peri, dperi (7 * 8 = 56 bytes doubles)
         * 
         * Total: 4 + 4 + 4 + 4 + 80 = 96 bytes minimum
         */
        private fun readSwissEphFormat(bytes: ByteArray, file: Path): EphemerisFileHeader {
            require(bytes.size >= 96) { 
                "SE1 file too small: $file (need at least 96 bytes, got ${bytes.size})" 
            }
            
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            
            // Read Swiss Ephemeris header
            val lndx0 = buffer.int            // File position of index
            val iflg = buffer.int             // Flags
            val ncoe = buffer.int             // Number of coefficients
            val rmaxInt = buffer.int          // Normalization * 1000
            
            // Read time range and segment info (10 doubles = 80 bytes)
            val tfstart = buffer.double       // Start Julian Day
            val tfend = buffer.double         // End Julian Day  
            val dseg = buffer.double          // Segment size (days)
            val telem = buffer.double         // Epoch of elements
            val prot = buffer.double
            val dprot = buffer.double
            val qrot = buffer.double
            val dqrot = buffer.double
            val peri = buffer.double
            val dperi = buffer.double
            
            // Validate
            require(ncoe > 0 && ncoe < 100) { 
                "Invalid coefficient count: $ncoe" 
            }
            require(tfstart > 0 && tfend > tfstart) { 
                "Invalid time range: $tfstart to $tfend" 
            }
            require(dseg > 0 && dseg < 10000) { 
                "Invalid segment size: $dseg" 
            }
            
            return EphemerisFileHeader(
                magic = "SE1\u0000",
                fileFormat = FileFormat.SE1,
                version = 1,
                startJD = tfstart,
                endJD = tfend,
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                numConstants = ncoe,
                astronomicalUnit = 149597870.7, // Standard AU value
                earthMoonRatio = 81.30056,      // Standard EMB ratio
                headerSize = 96 + if ((iflg and 2) != 0) ncoe * 2 * 8 else 0
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
