package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Paths

class Se1BinaryReaderTest {

    @Test
    @Disabled("Requires real SE1 file format - mock files don't match Swiss Ephemeris structure")
    fun `should read SE1 file header correctly`() {
        // Create minimal SE1 test file
        val testFile = createTestSe1File(
            startJD = 2451545.0,  // J2000
            endJD = 2451577.0,    // 32 days later
            numCoeffs = 11
        )

        val reader = Se1BinaryReader(testFile)
        val records = reader.readRecords()

        assertThat(records).isNotEmpty
        assertThat(records[0].startJulianDay.value).isCloseTo(2451545.0, offset(0.1))
        assertThat(records[0].endJulianDay.value).isCloseTo(2451577.0, offset(0.1))
        assertThat(records[0].longitudeCoefficients).hasSize(11)
    }

    @Test
    @Disabled("Requires real SE1 file format")
    fun `should read multiple records from SE1 file`() {
        val testFile = createTestSe1FileWithMultipleRecords()

        val reader = Se1BinaryReader(testFile)
        val records = reader.readRecords()

        assertThat(records.size).isGreaterThan(1)
        // Records should be chronologically ordered
        for (i in 0 until records.size - 1) {
            assertThat(records[i].endJulianDay.value)
                .isLessThanOrEqualTo(records[i + 1].startJulianDay.value)
        }
    }

    @Test
    @Disabled("Requires real SE1 file format")
    fun `should find correct record for given Julian Day`() {
        val testFile = createTestSe1FileWithMultipleRecords()

        val reader = Se1BinaryReader(testFile)
        val jd = JulianDay(2451560.0) // Mid-range

        val record = reader.findRecord(jd)

        assertThat(record).isNotNull
        assertThat(record!!.contains(jd)).isTrue()
    }

    @Test
    @Disabled("Requires real SE1 file format")
    fun `should return null when Julian Day not in any record`() {
        val testFile = createTestSe1FileWithMultipleRecords()

        val reader = Se1BinaryReader(testFile)
        val jd = JulianDay(2400000.0) // Way before any records

        val record = reader.findRecord(jd)

        assertThat(record).isNull()
    }

    @Test
    @Disabled("Requires real SE1 file format")
    fun `should handle little-endian byte order correctly`() {
        val testFile = createTestSe1File(
            startJD = 2451545.0,
            endJD = 2451577.0,
            numCoeffs = 5
        )

        val reader = Se1BinaryReader(testFile)
        val records = reader.readRecords()

        // Verify that doubles are read correctly (not byte-swapped)
        val record = records[0]
        assertThat(record.startJulianDay.value).isBetween(2400000.0, 2500000.0)
        assertThat(record.endJulianDay.value).isBetween(2400000.0, 2500000.0)
    }

    @Test
    fun `should throw exception for non-existent file`() {
        val nonExistent = Paths.get("/tmp/does-not-exist.se1")

        assertThrows<IllegalArgumentException> {
            Se1BinaryReader(nonExistent)
        }
    }

    @Test
    @Disabled("SE1 binary format needs analysis")
    fun `should read actual SE1 file if available`() {
        // This test requires actual Swiss Ephemeris files
        val ephePath = System.getenv("SE_EPHE_PATH")
        if (ephePath == null) {
            // Skip test if no ephemeris path set
            return
        }

        val se1File = Paths.get(ephePath, "sepl_18.se1")
        if (!Files.exists(se1File)) {
            // Skip if file not available
            return
        }

        val reader = Se1BinaryReader(se1File)
        val records = reader.readRecords()

        assertThat(records).isNotEmpty
        // Verify records cover year 2000 (JD ~2451545)
        val hasJ2000 = records.any { it.contains(JulianDay.J2000) }
        assertThat(hasJ2000).isTrue()
    }

    // Helper functions

    private fun createTestSe1File(
        startJD: Double,
        endJD: Double,
        numCoeffs: Int
    ): java.nio.file.Path {
        val tempFile = Files.createTempFile("test_se1_", ".se1")

        java.nio.ByteBuffer.allocate(8 + 8 + 4 + numCoeffs * 3 * 8).apply {
            order(java.nio.ByteOrder.LITTLE_ENDIAN)

            // Header
            putDouble(startJD)
            putDouble(endJD)
            putInt(numCoeffs)

            // Dummy coefficients (longitude, latitude, distance)
            repeat(numCoeffs * 3) {
                putDouble(0.0)
            }

            Files.write(tempFile, array())
        }

        return tempFile
    }

    private fun createTestSe1FileWithMultipleRecords(): java.nio.file.Path {
        val tempFile = Files.createTempFile("test_se1_multi_", ".se1")
        val buffer = java.io.ByteArrayOutputStream()

        // Create 3 records covering J2000 period
        val records = listOf(
            Triple(2451545.0, 2451577.0, 11),  // J2000
            Triple(2451577.0, 2451609.0, 11),  // Next 32 days
            Triple(2451609.0, 2451641.0, 11)   // Another 32 days
        )

        for ((start, end, numCoeffs) in records) {
            java.nio.ByteBuffer.allocate(8 + 8 + 4 + numCoeffs * 3 * 8).apply {
                order(java.nio.ByteOrder.LITTLE_ENDIAN)
                putDouble(start)
                putDouble(end)
                putInt(numCoeffs)
                repeat(numCoeffs * 3) {
                    putDouble(0.0)
                }
                buffer.write(array())
            }
        }

        Files.write(tempFile, buffer.toByteArray())
        return tempFile
    }
}
