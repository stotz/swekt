package ch.typedef.swekt.calculation

import ch.typedef.swekt.config.EphemerisConfig
import ch.typedef.swekt.io.Se1Record
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Swiss Ephemeris calculation engine.
 *
 * This engine uses Chebyshev interpolation on SE1 file data
 * to calculate high-precision planetary positions.
 */
class SwissEphemerisEngineTest {

    @Test
    fun `calculate position from SE1 record`() {
        // Create a test record with simple coefficients
        val startJd = JulianDay(2451545.0)  // J2000.0
        val endJd = JulianDay(2451577.0)     // 32 days later
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45, 16.21, -0.023),
            latitudeCoefficients = doubleArrayOf(1.85, 0.12),
            distanceCoefficients = doubleArrayOf(1.523, 0.0024)
        )
        
        val engine = SwissEphemerisEngine()
        
        // Calculate at midpoint
        val midJd = JulianDay(2451561.0)
        val position = engine.calculateFromRecord(Planet.MARS, midJd, record)
        
        assertEquals(Planet.MARS, position.planet)
        assertEquals(midJd, position.julianDay)
        assertTrue(position.longitude >= 0.0 && position.longitude <= 360.0)
        assertTrue(position.latitude >= -90.0 && position.latitude <= 90.0)
        assertTrue(position.distance > 0.0)
    }

    @Test
    fun `calculate at record boundaries`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45, 16.21),
            latitudeCoefficients = doubleArrayOf(1.85, 0.12),
            distanceCoefficients = doubleArrayOf(1.523, 0.0024)
        )
        
        val engine = SwissEphemerisEngine()
        
        // Should work at start
        val posStart = engine.calculateFromRecord(Planet.MARS, startJd, record)
        assertTrue(posStart.longitude.isFinite())
        
        // Should work at end
        val posEnd = engine.calculateFromRecord(Planet.MARS, endJd, record)
        assertTrue(posEnd.longitude.isFinite())
    }

    @Test
    fun `position changes over time`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        // Record with linear motion (16.21 degrees per normalized time)
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45, 16.21),
            latitudeCoefficients = doubleArrayOf(1.85, 0.0),
            distanceCoefficients = doubleArrayOf(1.523, 0.0)
        )
        
        val engine = SwissEphemerisEngine()
        
        val pos1 = engine.calculateFromRecord(Planet.MARS, startJd, record)
        val pos2 = engine.calculateFromRecord(Planet.MARS, endJd, record)
        
        // Longitude should have changed
        assertTrue(pos1.longitude != pos2.longitude)
    }

    @Test
    fun `velocity is calculated`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45, 16.21, -0.023),
            latitudeCoefficients = doubleArrayOf(1.85, 0.12, -0.002),
            distanceCoefficients = doubleArrayOf(1.523, 0.0024, -0.00001)
        )
        
        val engine = SwissEphemerisEngine()
        val midJd = JulianDay(2451561.0)
        val position = engine.calculateFromRecord(Planet.MARS, midJd, record)
        
        // Speed should be calculated
        assertTrue(position.longitudeSpeed.isFinite())
        assertTrue(position.latitudeSpeed.isFinite())
        assertTrue(position.distanceSpeed.isFinite())
        
        // Mars moves roughly 0.5 degrees per day
        assertTrue(position.longitudeSpeed > 0.0)
        assertTrue(position.longitudeSpeed < 2.0)
    }

    @Test
    fun `throws on Julian day outside record range`() {
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45),
            latitudeCoefficients = doubleArrayOf(1.85),
            distanceCoefficients = doubleArrayOf(1.523)
        )
        
        val engine = SwissEphemerisEngine()
        
        // Before range
        assertThrows<IllegalArgumentException> {
            engine.calculateFromRecord(Planet.MARS, JulianDay(2451544.0), record)
        }
        
        // After range
        assertThrows<IllegalArgumentException> {
            engine.calculateFromRecord(Planet.MARS, JulianDay(2451578.0), record)
        }
    }

    @Test
    fun `works with single coefficient`() {
        // Constant position (no motion)
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(355.45),
            latitudeCoefficients = doubleArrayOf(1.85),
            distanceCoefficients = doubleArrayOf(1.523)
        )
        
        val engine = SwissEphemerisEngine()
        val midJd = JulianDay(2451561.0)
        val position = engine.calculateFromRecord(Planet.MARS, midJd, record)
        
        // Position should be close to coefficient value
        assertEquals(355.45, position.longitude, 0.1)
        assertEquals(1.85, position.latitude, 0.1)
        assertEquals(1.523, position.distance, 0.01)
        
        // No motion
        assertEquals(0.0, position.longitudeSpeed, 0.001)
    }

    @Test
    fun `works with many coefficients`() {
        // High-order polynomial
        val startJd = JulianDay(2451545.0)
        val endJd = JulianDay(2451577.0)
        
        val record = Se1Record(
            startJulianDay = startJd,
            endJulianDay = endJd,
            longitudeCoefficients = doubleArrayOf(
                355.45, 16.21, -0.023, 0.001, -0.00002, 0.0000001
            ),
            latitudeCoefficients = doubleArrayOf(
                1.85, 0.12, -0.002, 0.00001
            ),
            distanceCoefficients = doubleArrayOf(
                1.523, 0.0024, -0.00001, 0.0000001
            )
        )
        
        val engine = SwissEphemerisEngine()
        val midJd = JulianDay(2451561.0)
        val position = engine.calculateFromRecord(Planet.MARS, midJd, record)
        
        // Should handle many terms without error
        assertTrue(position.longitude.isFinite())
        assertTrue(position.latitude.isFinite())
        assertTrue(position.distance.isFinite())
    }
}
