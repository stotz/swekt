package ch.typedef.swekt.calculation

import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlanetaryPositionTest {

    @Test
    fun `create planetary position`() {
        val jd = JulianDay(2451545.0) // J2000.0
        val position = PlanetaryPosition(
            planet = Planet.MARS,
            julianDay = jd,
            longitude = 355.45,
            latitude = 1.85,
            distance = 1.38,
            longitudeSpeed = 0.5,
            latitudeSpeed = 0.01,
            distanceSpeed = -0.001
        )

        assertEquals(Planet.MARS, position.planet)
        assertEquals(jd, position.julianDay)
        assertEquals(355.45, position.longitude, 0.001)
        assertEquals(1.85, position.latitude, 0.001)
        assertEquals(1.38, position.distance, 0.001)
        assertEquals(0.5, position.longitudeSpeed, 0.001)
        assertEquals(0.01, position.latitudeSpeed, 0.001)
        assertEquals(-0.001, position.distanceSpeed, 0.0001)
    }

    @Test
    fun `longitude must be 0 to 360`() {
        val jd = JulianDay(2451545.0)

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = -10.0,
                latitude = 0.0,
                distance = 1.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = 361.0,
                latitude = 0.0,
                distance = 1.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }
    }

    @Test
    fun `latitude must be -90 to 90`() {
        val jd = JulianDay(2451545.0)

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = 0.0,
                latitude = -91.0,
                distance = 1.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = 0.0,
                latitude = 91.0,
                distance = 1.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }
    }

    @Test
    fun `distance must be positive`() {
        val jd = JulianDay(2451545.0)

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = 0.0,
                latitude = 0.0,
                distance = -1.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }

        assertThrows<IllegalArgumentException> {
            PlanetaryPosition(
                planet = Planet.MARS,
                julianDay = jd,
                longitude = 0.0,
                latitude = 0.0,
                distance = 0.0,
                longitudeSpeed = 0.0,
                latitudeSpeed = 0.0,
                distanceSpeed = 0.0
            )
        }
    }

    @Test
    fun `planetary position is immutable`() {
        val jd = JulianDay(2451545.0)
        val pos1 = PlanetaryPosition(
            planet = Planet.MARS,
            julianDay = jd,
            longitude = 100.0,
            latitude = 1.0,
            distance = 1.5,
            longitudeSpeed = 0.5,
            latitudeSpeed = 0.01,
            distanceSpeed = 0.0
        )

        // Data class generates equals/hashCode
        val pos2 = PlanetaryPosition(
            planet = Planet.MARS,
            julianDay = jd,
            longitude = 100.0,
            latitude = 1.0,
            distance = 1.5,
            longitudeSpeed = 0.5,
            latitudeSpeed = 0.01,
            distanceSpeed = 0.0
        )

        assertEquals(pos1, pos2)
        assertEquals(pos1.hashCode(), pos2.hashCode())
    }

    @Test
    fun `toString includes all fields`() {
        val jd = JulianDay(2451545.0)
        val position = PlanetaryPosition(
            planet = Planet.MARS,
            julianDay = jd,
            longitude = 355.45,
            latitude = 1.85,
            distance = 1.38,
            longitudeSpeed = 0.5,
            latitudeSpeed = 0.01,
            distanceSpeed = -0.001
        )

        val str = position.toString()
        assertTrue(str.contains("MARS"), "toString should contain planet name")
        assertTrue(str.contains("355.45"), "toString should contain longitude")
        assertTrue(str.contains("1.85"), "toString should contain latitude")
    }
}
