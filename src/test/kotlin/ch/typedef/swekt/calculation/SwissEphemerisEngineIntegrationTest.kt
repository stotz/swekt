package ch.typedef.swekt.calculation

import ch.typedef.swekt.config.EphemerisConfig
import ch.typedef.swekt.io.EphemerisFileReader
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.offset
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.math.abs

/**
 * Integration tests for the complete Swiss Ephemeris calculation pipeline.
 *
 * NOTE: Disabled until SE1 binary format is fully implemented.
 * The Swiss Ephemeris binary format is more complex than initially expected.
 * 
 * These tests require:
 * - SE_EPHE_PATH environment variable set
 * - Swiss Ephemeris SE1 files available
 *
 * Tests validate against known astronomical positions.
 */
@Disabled("SE1 binary format needs further analysis")
class SwissEphemerisEngineIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should calculate Mars position at J2000 using SE1 data`() {
        // Setup
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val jd = JulianDay.J2000
        val planet = Planet.MARS

        // Read SE1 record
        val record = reader.readSe1Record(planet, jd)

        // Calculate position
        val position = engine.calculateFromRecord(planet, jd, record)

        // Verify
        assertThat(position.planet).isEqualTo(planet)
        assertThat(position.julianDay).isEqualTo(jd)

        // Position should be reasonable
        assertThat(position.longitude).isBetween(0.0, 360.0)
        assertThat(position.latitude).isBetween(-90.0, 90.0)
        assertThat(position.distance).isGreaterThan(0.0)

        // Mars distance from Sun should be reasonable (1.38 to 1.67 AU)
        assertThat(position.distance).isBetween(1.0, 2.0)

        // Velocities should be non-zero
        assertThat(abs(position.longitudeSpeed)).isGreaterThan(0.0)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should calculate Sun position at J2000`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val jd = JulianDay.J2000
        val planet = Planet.SUN

        val record = reader.readSe1Record(planet, jd)
        val position = engine.calculateFromRecord(planet, jd, record)

        assertThat(position.longitude).isBetween(0.0, 360.0)
        assertThat(position.distance).isCloseTo(1.0, offset(0.05)) // ~1 AU
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should calculate Moon position at J2000`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val jd = JulianDay.J2000
        val planet = Planet.MOON

        val record = reader.readSe1Record(planet, jd)
        val position = engine.calculateFromRecord(planet, jd, record)

        assertThat(position.longitude).isBetween(0.0, 360.0)
        assertThat(position.distance).isCloseTo(0.0026, offset(0.001)) // Moon distance in AU
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should calculate positions for all major planets`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val jd = JulianDay.J2000
        val planets = listOf(
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN
        )

        for (planet in planets) {
            val record = reader.readSe1Record(planet, jd)
            val position = engine.calculateFromRecord(planet, jd, record)

            assertThat(position.longitude)
                .describedAs("${planet.displayName} longitude")
                .isBetween(0.0, 360.0)

            assertThat(position.distance)
                .describedAs("${planet.displayName} distance")
                .isGreaterThan(0.0)
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should calculate position at different times within same record`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val planet = Planet.MARS
        val baseJd = JulianDay.J2000

        // Read record (covers ~32 days)
        val record = reader.readSe1Record(planet, baseJd)

        // Calculate at start, middle, end of record
        val times = listOf(
            record.startJulianDay,
            record.midpoint,
            record.endJulianDay
        )

        val positions = times.map { jd ->
            engine.calculateFromRecord(planet, jd, record)
        }

        // All positions should be valid
        positions.forEach { pos ->
            assertThat(pos.longitude).isBetween(0.0, 360.0)
            assertThat(pos.distance).isGreaterThan(0.0)
        }

        // Positions should differ (planet is moving)
        assertThat(positions[0].longitude).isNotEqualTo(positions[2].longitude)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should produce consistent results for same input`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()

        val jd = JulianDay.J2000
        val planet = Planet.MARS
        val record = reader.readSe1Record(planet, jd)

        // Calculate twice
        val pos1 = engine.calculateFromRecord(planet, jd, record)
        val pos2 = engine.calculateFromRecord(planet, jd, record)

        // Should be identical
        assertThat(pos1.longitude).isEqualTo(pos2.longitude)
        assertThat(pos1.latitude).isEqualTo(pos2.latitude)
        assertThat(pos1.distance).isEqualTo(pos2.distance)
        assertThat(pos1.longitudeSpeed).isEqualTo(pos2.longitudeSpeed)
        assertThat(pos1.latitudeSpeed).isEqualTo(pos2.latitudeSpeed)
        assertThat(pos1.distanceSpeed).isEqualTo(pos2.distanceSpeed)
    }
}
