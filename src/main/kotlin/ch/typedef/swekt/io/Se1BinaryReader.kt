package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads Swiss Ephemeris SE1 binary files.
 *
 * SE1 Format Structure:
 * 1. Header (96+ bytes): File metadata, time range, coefficients info
 * 2. Index: Array of segment start positions
 * 3. Segments: Each segment contains Chebyshev coefficients for a time span
 *
 * This implementation reads the segments after the index.
 *
 * @property path Path to SE1 file
 */
class Se1BinaryReader(private val path: Path) {

    private data class FileHeader(
        val lndx0: Int,          // Index position in file
        val iflg: Int,           // Flags
        val ncoe: Int,           // Number of coefficients
        val tfstart: Double,     // Start JD
        val tfend: Double,       // End JD
        val dseg: Double,        // Segment size (days)
        val nndx: Int            // Number of index entries
    )

    init {
        require(Files.exists(path)) {
            "SE1 file not found: $path"
        }
        require(Files.isRegularFile(path)) {
            "Not a regular file: $path"
        }
    }

    /**
     * Reads all records (segments) from the SE1 file.
     *
     * @return List of SE1 records in chronological order
     */
    fun readRecords(): List<Se1Record> {
        val bytes = Files.readAllBytes(path)
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

        // Read file header
        val header = readFileHeader(buffer)

        // Read index (array of int32 positions)
        buffer.position(header.lndx0)
        val indexPositions = IntArray(header.nndx) { buffer.int }

        // Read segments
        val records = mutableListOf<Se1Record>()
        
        for (i in 0 until header.nndx) {
            val segmentPos = indexPositions[i]
            if (segmentPos <= 0 || segmentPos >= bytes.size) {
                break // Invalid position
            }

            buffer.position(segmentPos)
            
            try {
                val record = readSegment(buffer, header)
                records.add(record)
            } catch (e: Exception) {
                // End of valid segments
                break
            }
        }

        return records
    }

    /**
     * Finds the record containing the given Julian Day.
     *
     * @param julianDay Time to search for
     * @return Record containing julianDay, or null if not found
     */
    fun findRecord(julianDay: JulianDay): Se1Record? {
        return readRecords().find { it.contains(julianDay) }
    }

    /**
     * Reads the file header (first 96 bytes).
     */
    private fun readFileHeader(buffer: ByteBuffer): FileHeader {
        val lndx0 = buffer.int
        val iflg = buffer.int
        val ncoe = buffer.int
        buffer.int // rmax (skip for now)

        val tfstart = buffer.double
        val tfend = buffer.double
        val dseg = buffer.double
        
        // Skip remaining orbital elements (7 doubles = 56 bytes)
        buffer.position(buffer.position() + 56)

        // Calculate number of segments
        val nndx = ((tfend - tfstart + 0.1) / dseg).toInt()

        return FileHeader(lndx0, iflg, ncoe, tfstart, tfend, dseg, nndx)
    }

    /**
     * Reads a single segment at current buffer position.
     */
    private fun readSegment(buffer: ByteBuffer, header: FileHeader): Se1Record {
        // Each segment starts with time range (2 doubles)
        val tseg0 = buffer.double
        val tseg1 = buffer.double

        // Read Chebyshev coefficients (3 coordinates * ncoe)
        val longitudeCoeffs = DoubleArray(header.ncoe) { buffer.double }
        val latitudeCoeffs = DoubleArray(header.ncoe) { buffer.double }
        val distanceCoeffs = DoubleArray(header.ncoe) { buffer.double }

        return Se1Record(
            startJulianDay = JulianDay(tseg0),
            endJulianDay = JulianDay(tseg1),
            longitudeCoefficients = longitudeCoeffs,
            latitudeCoefficients = latitudeCoeffs,
            distanceCoefficients = distanceCoeffs
        )
    }
}
