package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for SE1 Chebyshev records.
 *
 * SE1 files store Chebyshev polynomial coefficients for planetary positions.
 * Each record covers a time span (typically 32 days) and contains coefficients
 * for longitude, latitude, and distance.
 */
class Se1RecordTest {

    @Test
    fun `create SE1 record`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val longitudeCoeffs = doubleArrayOf(355.45, 16.21, -0.023, 0.001)
        val latitudeCoeffs = doubleArrayOf(1.85, 0.12, -0.002)
        val distanceCoeffs = doubleArrayOf(1.523, 0.0024, -0.00001)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = longitudeCoeffs,
            latitudeCoefficients = latitudeCoeffs,
            distanceCoefficients = distanceCoeffs
        )
        
        assertEquals(startJd, record.startJulianDay)
        assertEquals(endJd, record.endJulianDay)
        assertEquals(4, record.longitudeCoefficients.size)
        assertEquals(3, record.latitudeCoefficients.size)
        assertEquals(3, record.distanceCoefficients.size)
    }

    @Test
    fun `record spans time range`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(1.0),
            latitudeCoefficients = doubleArrayOf(0.0),
            distanceCoefficients = doubleArrayOf(1.0)
        )
        
        // Typically 32 days
        val span = endJd.value - startJd.value
        assertEquals(32.0, span, 0.001)
    }

    @Test
    fun `check if Julian day is in range`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(1.0),
            latitudeCoefficients = doubleArrayOf(0.0),
            distanceCoefficients = doubleArrayOf(1.0)
        )
        
        // Before range
        assertTrue(!record.contains(JulianDay(2451544.0)))
        
        // At start (inclusive)
        assertTrue(record.contains(JulianDay(2451545.0)))
        
        // In middle
        assertTrue(record.contains(JulianDay(2451560.0)))
        
        // At end (inclusive)
        assertTrue(record.contains(JulianDay(2451577.0)))
        
        // After range
        assertTrue(!record.contains(JulianDay(2451578.0)))
    }

    @Test
    fun `get time span`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(1.0),
            latitudeCoefficients = doubleArrayOf(0.0),
            distanceCoefficients = doubleArrayOf(1.0)
        )
        
        assertEquals(32.0, record.timeSpan, 0.001)
    }

    @Test
    fun `get midpoint`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(1.0),
            latitudeCoefficients = doubleArrayOf(0.0),
            distanceCoefficients = doubleArrayOf(1.0)
        )
        
        val midpoint = record.midpoint
        assertEquals(2451561.0, midpoint.value, 0.001)
    }

    @Test
    fun `coefficients can be empty`() {
        // For some calculations, coefficients might be zero or minimal
        val record = Se1Record(
            startJulianDay = JulianDay(2451545.0),
            endJulianDay = JulianDay(2451577.0),
            longitudeCoefficients = doubleArrayOf(),
            latitudeCoefficients = doubleArrayOf(),
            distanceCoefficients = doubleArrayOf()
        )
        
        assertEquals(0, record.longitudeCoefficients.size)
    }

    @Test
    fun `different coefficient counts are allowed`() {
        // Different coordinates may have different numbers of coefficients
        val record = Se1Record(
            startJulianDay = JulianDay(2451545.0),
            endJulianDay = JulianDay(2451577.0),
            longitudeCoefficients = doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0),
            latitudeCoefficients = doubleArrayOf(1.0, 2.0),
            distanceCoefficients = doubleArrayOf(1.0, 2.0, 3.0)
        )
        
        assertEquals(5, record.longitudeCoefficients.size)
        assertEquals(2, record.latitudeCoefficients.size)
        assertEquals(3, record.distanceCoefficients.size)
    }
}
