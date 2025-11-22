package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads NASA JPL Ephemeris files (DE200, DE405, DE406, DE431, DE441, etc.)
 *
 * JPL ephemeris files contain planetary positions as Chebyshev polynomial coefficients.
 * This format is simpler and better documented than Swiss Ephemeris SE1 format.
 *
 * File Format (based on swejpl.c):
 * - Record 1: Header with constants, astronomical data, and index
 * - Record 2+: Coefficient blocks for time intervals
 *
 * References:
 * - swejpl.c from Swiss Ephemeris
 * - NASA JPL Ephemeris documentation
 *
 * @property path Path to JPL ephemeris file (e.g., de441.eph)
 */
class JplEphemerisReader(private val path: Path) {

    /**
     * JPL ephemeris header information.
     */
    data class JplHeader(
        val title: String,
        val deNumber: Int,
        val startJD: Double,
        val endJD: Double,
        val intervalDays: Double,
        val astronomicalUnit: Double,
        val earthMoonRatio: Double,
        val numConstants: Int,
        val constantNames: List<String>,
        val constantValues: List<Double>,
        val indexTable: IntArray,  // ipt[39]: planet data indices
        val recordSize: Int,       // Size of each data record in bytes
        val needsByteSwap: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as JplHeader
            return indexTable.contentEquals(other.indexTable)
        }

        override fun hashCode(): Int = indexTable.contentHashCode()
    }

    init {
        require(Files.exists(path)) {
            "JPL ephemeris file not found: $path"
        }
        require(Files.isRegularFile(path)) {
            "Not a regular file: $path"
        }
    }

    /**
     * Reads and parses the JPL ephemeris file header (record 1).
     *
     * @return Parsed header with all metadata
     */
    fun readHeader(): JplHeader {
        val buffer = ByteBuffer.wrap(Files.readAllBytes(path))
        
        // Read title (252 bytes = 3 lines of 84 chars)
        val titleBytes = ByteArray(252)
        buffer.get(titleBytes)
        val title = String(titleBytes, Charsets.US_ASCII).trim()
        
        // Read constant names (6 * 400 = 2400 bytes)
        val constantNamesBytes = ByteArray(2400)
        buffer.get(constantNamesBytes)
        val constantNames = mutableListOf<String>()
        for (i in 0 until 400) {
            val name = String(constantNamesBytes, i * 6, 6, Charsets.US_ASCII).trim()
            if (name.isNotEmpty()) {
                constantNames.add(name)
            }
        }
        
        // Read temporal range (3 doubles)
        buffer.order(ByteOrder.LITTLE_ENDIAN)  // Try little-endian first
        val startJD = buffer.double
        val endJD = buffer.double
        val intervalDays = buffer.double
        
        // Check if byte swapping is needed
        val needsByteSwap = when {
            intervalDays < 1.0 || intervalDays > 200.0 -> {
                // Invalid interval - try byte swapping
                buffer.position(2652)
                buffer.order(ByteOrder.BIG_ENDIAN)
                true
            }
            else -> false
        }
        
        // Re-read with correct endianness if needed
        val (finalStartJD, finalEndJD, finalInterval) = if (needsByteSwap) {
            buffer.position(2652)
            Triple(buffer.double, buffer.double, buffer.double)
        } else {
            Triple(startJD, endJD, intervalDays)
        }
        
        // Number of constants
        val numConstants = buffer.int
        
        // Astronomical unit
        val au = buffer.double
        
        // Earth/Moon mass ratio
        val emrat = buffer.double
        
        // Index table (36 int32 values)
        val ipt = IntArray(36) { buffer.int }
        
        // DE number
        val deNumber = buffer.int
        
        // Libration pointers (3 int32 values) - append to ipt[]
        val librationPtrs = IntArray(3) { buffer.int }
        val fullIpt = ipt + librationPtrs  // Total 39 values
        
        // Read constant values (they're at the end of record 1)
        // Skip to after first record structure, then read ncon doubles
        buffer.position(0)
        val record1Size = calculateRecordSize(fullIpt, deNumber)
        
        // Constants are after the index data
        val constantValues = mutableListOf<Double>()
        // For now, we'll read them when needed from the actual positions
        
        return JplHeader(
            title = title,
            deNumber = deNumber,
            startJD = finalStartJD,
            endJD = finalEndJD,
            intervalDays = finalInterval,
            astronomicalUnit = au,
            earthMoonRatio = emrat,
            numConstants = numConstants,
            constantNames = constantNames.take(numConstants),
            constantValues = constantValues,
            indexTable = fullIpt,
            recordSize = record1Size * 4,  // Convert from float count to bytes
            needsByteSwap = needsByteSwap
        )
    }

    /**
     * Calculates the record size based on the index table.
     *
     * This follows the algorithm in swejpl.c fsizer().
     * 
     * The record size is computed from the last body in the index table.
     * Each body has 3 values in ipt[]:
     *   ipt[i*3 + 0] = starting position in buffer (1-based)
     *   ipt[i*3 + 1] = number of coefficients per component
     *   ipt[i*3 + 2] = number of intervals in record
     */
    private fun calculateRecordSize(ipt: IntArray, deNumber: Int): Int {
        // Find the body with the highest starting position
        var kmx = 0  // Maximum starting position
        var khi = 0  // Index of body with max position
        
        for (i in 0 until 13) {
            val startPos = ipt[i * 3]
            if (startPos > kmx) {
                kmx = startPos
                khi = i + 1
            }
        }
        
        // Determine number of dimensions
        // Body 12 (nutations) has only 2 components (longitude and obliquity)
        // All other bodies have 3 components (x, y, z)
        val nd = if (khi == 12) 2 else 3
        
        // Calculate total size in doubles
        // Formula: (start_pos + num_components * num_coeffs * num_intervals - 1)
        // The index is 1-based, so we need the -1
        val idx = khi * 3 - 3  // Index for the last body
        val startPos = ipt[idx]
        val numCoeffs = ipt[idx + 1]
        val numIntervals = ipt[idx + 2]
        
        var numDoubles = startPos + nd * numCoeffs * numIntervals - 1
        
        // Convert to number of 4-byte words (for compatibility with C code)
        var ksize = numDoubles * 2
        
        // Special case for DE102 (has padding to match DE200 length)
        if (ksize == 1546) {
            ksize = 1652
        }
        
        return ksize
    }

    /**
     * Finds which data record contains the given Julian Day.
     *
     * @param julianDay Time to search for
     * @param header File header with time range info
     * @return Record number (0-based, where 0 is first data record after header)
     */
    fun findRecordNumber(julianDay: JulianDay, header: JplHeader): Int {
        require(julianDay.value >= header.startJD && julianDay.value <= header.endJD) {
            "Julian Day ${julianDay.value} is outside ephemeris range " +
            "[${header.startJD}, ${header.endJD}]"
        }
        
        // Calculate which interval this JD falls into
        val daysSinceStart = julianDay.value - header.startJD
        val recordNum = (daysSinceStart / header.intervalDays).toInt()
        
        return recordNum
    }

    /**
     * Reads a specific data record from the file.
     *
     * @param recordNum Record number (0 = first record after header)
     * @param header File header
     * @return Array of coefficient data
     */
    fun readRecord(recordNum: Int, header: JplHeader): DoubleArray {
        // Record 1 is the header, data records start at record 2
        // Each record is header.recordSize bytes
        
        // Skip header (we need to know its exact size)
        // For now, use a standard size based on DE number
        val headerSize = when (header.deNumber) {
            in 400..499 -> 8144  // Modern ephemerides
            200 -> 6608
            102 -> 6608
            else -> 8144
        }
        
        val recordOffset = headerSize + (recordNum * header.recordSize)
        
        val bytes = Files.readAllBytes(path)
        require(recordOffset + header.recordSize <= bytes.size) {
            "Record $recordNum would exceed file size"
        }
        
        val buffer = ByteBuffer.wrap(bytes, recordOffset, header.recordSize)
        buffer.order(if (header.needsByteSwap) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)
        
        // Read as doubles
        val numDoubles = header.recordSize / 8
        val data = DoubleArray(numDoubles) { buffer.double }
        
        return data
    }

    /**
     * JPL body indices (as defined in swejpl.h).
     */
    enum class JplBody(val index: Int) {
        MERCURY(0),
        VENUS(1),
        EARTH(2),
        MARS(3),
        JUPITER(4),
        SATURN(5),
        URANUS(6),
        NEPTUNE(7),
        PLUTO(8),
        MOON(9),
        SUN(10),
        SOLAR_SYSTEM_BARYCENTER(11),
        EARTH_MOON_BARYCENTER(12),
        NUTATIONS(13),
        LIBRATIONS(14)
    }

    /**
     * Extracts Chebyshev coefficients for a specific body from a data record.
     *
     * @param record Data record (from readRecord)
     * @param body Which celestial body
     * @param header File header with index table
     * @return ChebyshevData with coefficients for x, y, z components
     */
    data class ChebyshevData(
        val startJD: Double,
        val endJD: Double,
        val coefficientsX: DoubleArray,
        val coefficientsY: DoubleArray,
        val coefficientsZ: DoubleArray,
        val numCoefficients: Int,
        val numIntervals: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ChebyshevData
            return coefficientsX.contentEquals(other.coefficientsX) &&
                   coefficientsY.contentEquals(other.coefficientsY) &&
                   coefficientsZ.contentEquals(other.coefficientsZ)
        }

        override fun hashCode(): Int {
            var result = coefficientsX.contentHashCode()
            result = 31 * result + coefficientsY.contentHashCode()
            result = 31 * result + coefficientsZ.contentHashCode()
            return result
        }
    }

    /**
     * Extracts coefficients for a body at a specific time within a record.
     *
     * @param record The data record
     * @param body Which body to extract
     * @param julianDay The time point
     * @param header File header
     * @return Chebyshev coefficients for the sub-interval containing julianDay
     */
    fun extractCoefficients(
        record: DoubleArray,
        body: JplBody,
        julianDay: JulianDay,
        header: JplHeader
    ): ChebyshevData {
        // First two doubles are the time span of this record
        val recordStart = record[0]
        val recordEnd = record[1]
        
        require(julianDay.value >= recordStart && julianDay.value <= recordEnd) {
            "JD ${julianDay.value} not in record range [$recordStart, $recordEnd]"
        }
        
        // Get index table info for this body
        val bodyIdx = body.index
        val ipt = header.indexTable
        
        val startPos = ipt[bodyIdx * 3] - 1  // Convert to 0-based index
        val numCoeffs = ipt[bodyIdx * 3 + 1]
        val numIntervals = ipt[bodyIdx * 3 + 2]
        
        // Determine number of components (nutations have 2, others have 3)
        val numComponents = if (body == JplBody.NUTATIONS) 2 else 3
        
        // Calculate which sub-interval contains the requested time
        val intervalDuration = (recordEnd - recordStart) / numIntervals
        val intervalIndex = ((julianDay.value - recordStart) / intervalDuration).toInt()
            .coerceIn(0, numIntervals - 1)
        
        // Calculate sub-interval time range
        val subIntervalStart = recordStart + intervalIndex * intervalDuration
        val subIntervalEnd = subIntervalStart + intervalDuration
        
        // Extract coefficients for this sub-interval
        // Layout: all X coeffs for interval, then all Y coeffs, then all Z coeffs
        val baseOffset = startPos + intervalIndex * numCoeffs * numComponents
        
        val coeffsX = DoubleArray(numCoeffs) { i ->
            record[baseOffset + i]
        }
        
        val coeffsY = DoubleArray(numCoeffs) { i ->
            record[baseOffset + numCoeffs + i]
        }
        
        val coeffsZ = if (numComponents == 3) {
            DoubleArray(numCoeffs) { i ->
                record[baseOffset + 2 * numCoeffs + i]
            }
        } else {
            DoubleArray(0)  // No Z component for nutations
        }
        
        return ChebyshevData(
            startJD = subIntervalStart,
            endJD = subIntervalEnd,
            coefficientsX = coeffsX,
            coefficientsY = coeffsY,
            coefficientsZ = coeffsZ,
            numCoefficients = numCoeffs,
            numIntervals = numIntervals
        )
    }
}
