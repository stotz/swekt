package ch.typedef.swekt.calculation

import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleCalculationEngineTest {

    private val engine = SimpleCalculationEngine()

    @Test
    fun `calculate Sun position at J2000`() {
        val jd = JulianDay.J2000
        val position = engine.calculate(Planet.SUN, jd)

        assertEquals(Planet.SUN, position.planet)
        assertEquals(jd, position.julianDay)

        // Sun at J2000.0 should be around 280 degrees longitude
        assertTrue(position.longitude in 270.0..290.0,
            "Expected Sun longitude ~280°, got ${position.longitude}")

        // Sun is always at ecliptic latitude 0
        assertEquals(0.0, position.latitude, 0.001)

        // Sun distance should be around 1 AU
        assertTrue(position.distance in 0.98..1.02,
            "Expected distance ~1 AU, got ${position.distance}")

        // Sun moves about 1 degree per day
        assertTrue(position.longitudeSpeed in 0.9..1.1,
            "Expected speed ~1°/day, got ${position.longitudeSpeed}")
    }

    @Test
    fun `calculate Sun position on specific date`() {
        // 2024-06-21 (Summer solstice) - Sun near 90 degrees
        val jd = JulianDay.fromGregorian(2024, 6, 21, 12.0)
        val position = engine.calculate(Planet.SUN, jd)

        // Sun should be near 90 degrees (Cancer start)
        assertTrue(position.longitude in 85.0..95.0,
            "Expected Sun near 90° at summer solstice, got ${position.longitude}")
    }

    @Test
    fun `calculate Sun position throughout year`() {
        val year = 2024
        val positions = mutableListOf<PlanetaryPosition>()

        // Calculate every month
        for (month in 1..12) {
            val jd = JulianDay.fromGregorian(year, month, 15, 12.0)
            val pos = engine.calculate(Planet.SUN, jd)
            positions.add(pos)
        }

        // Sun should complete roughly 360 degrees in a year
        val firstLon = positions.first().longitude
        val lastLon = positions.last().longitude

        // Longitude should increase (accounting for 0/360 wrap)
        // We just check that all positions are valid
        positions.forEach { pos ->
            assertTrue(pos.longitude in 0.0..360.0)
            assertEquals(0.0, pos.latitude, 0.001)
            assertTrue(pos.distance > 0.0)
        }
    }

    @Test
    fun `calculate Moon position at J2000`() {
        val jd = JulianDay.J2000
        val position = engine.calculate(Planet.MOON, jd)

        assertEquals(Planet.MOON, position.planet)
        assertEquals(jd, position.julianDay)

        // Moon should be at valid position
        assertTrue(position.longitude in 0.0..360.0)
        assertTrue(position.latitude in -10.0..10.0,
            "Moon latitude should be within ~±5°")

        // Moon distance should be around 0.0026 AU (Earth-Moon distance)
        assertTrue(position.distance in 0.0020..0.0030,
            "Expected Moon distance ~0.0026 AU, got ${position.distance}")

        // Moon moves about 13 degrees per day
        assertTrue(position.longitudeSpeed in 11.0..15.0,
            "Expected Moon speed ~13°/day, got ${position.longitudeSpeed}")
    }

    @Test
    fun `unsupported planets throw exception`() {
        val jd = JulianDay.J2000

        // Only Sun and Moon are supported initially
        assertThrows<UnsupportedOperationException> {
            engine.calculate(Planet.MERCURY, jd)
        }

        assertThrows<UnsupportedOperationException> {
            engine.calculate(Planet.MARS, jd)
        }

        assertThrows<UnsupportedOperationException> {
            engine.calculate(Planet.JUPITER, jd)
        }
    }

    @Test
    fun `calculation is deterministic`() {
        val jd = JulianDay(2451545.0)

        val pos1 = engine.calculate(Planet.SUN, jd)
        val pos2 = engine.calculate(Planet.SUN, jd)

        assertEquals(pos1, pos2, "Same inputs should produce same results")
    }

    @Test
    fun `sun longitude increases over time`() {
        val jd1 = JulianDay(2451545.0)
        val jd2 = jd1.plus(30.0) // 30 days later

        val pos1 = engine.calculate(Planet.SUN, jd1)
        val pos2 = engine.calculate(Planet.SUN, jd2)

        // Sun should move forward approximately 30 degrees in 30 days
        var lonDiff = pos2.longitude - pos1.longitude
        if (lonDiff < 0) lonDiff += 360.0 // Handle 0/360 wrap

        assertTrue(lonDiff in 25.0..35.0,
            "Expected ~30° movement, got $lonDiff")
    }

    @Test
    fun `moon moves faster than sun`() {
        val jd = JulianDay(2451545.0)

        val sunPos = engine.calculate(Planet.SUN, jd)
        val moonPos = engine.calculate(Planet.MOON, jd)

        assertTrue(moonPos.longitudeSpeed > sunPos.longitudeSpeed * 10,
            "Moon should move much faster than Sun")
    }
}
