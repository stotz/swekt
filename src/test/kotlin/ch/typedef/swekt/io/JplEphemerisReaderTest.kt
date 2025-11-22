package ch.typedef.swekt.io

import ch.typedef.swekt.engine.JplCalculationEngine
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.nio.file.Paths
import kotlin.math.sqrt

/**
 * Tests for JPL Ephemeris file reading and calculations.
 *
 * These tests require JPL_EPHE_PATH environment variable pointing to JPL files.
 */
class JplEphemerisReaderTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should read JPL file header correctly`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        
        // Try DE441 first
        val de441 = Paths.get(ephePath, "de441.eph")
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val reader = JplEphemerisReader(de441)
        val header = reader.readHeader()
        
        // Verify basic header info
        assertThat(header.deNumber).isEqualTo(441)
        assertThat(header.title).contains("JPL")
        
        // Time range should be reasonable
        assertThat(header.startJD).isGreaterThan(0.0)
        assertThat(header.endJD).isGreaterThan(header.startJD)
        assertThat(header.intervalDays).isBetween(1.0, 200.0)
        
        // Physical constants should be reasonable
        assertThat(header.astronomicalUnit).isBetween(1.49e11, 1.50e11)  // meters
        assertThat(header.earthMoonRatio).isBetween(80.0, 82.0)
        
        // Should have constant names
        assertThat(header.constantNames).isNotEmpty
        assertThat(header.numConstants).isGreaterThan(0)
        
        // Index table should be populated
        assertThat(header.indexTable).hasSize(39)
        assertThat(header.indexTable[0]).isGreaterThan(0)  // Mercury starts somewhere
        
        println("DE441 Header:")
        println("  Title: ${header.title}")
        println("  DE Number: ${header.deNumber}")
        println("  Time range: ${header.startJD} - ${header.endJD} JD")
        println("  Interval: ${header.intervalDays} days")
        println("  AU: ${header.astronomicalUnit} m")
        println("  Earth/Moon ratio: ${header.earthMoonRatio}")
        println("  Constants: ${header.numConstants}")
        println("  Record size: ${header.recordSize} bytes")
        println("  Byte swap needed: ${header.needsByteSwap}")
        println("  Index table (first 12 values):")
        for (i in 0 until 12 step 3) {
            println("    Planet ${i/3}: offset=${header.indexTable[i]}, " +
                   "ncf=${header.indexTable[i+1]}, niv=${header.indexTable[i+2]}")
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should find correct record for J2000`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val reader = JplEphemerisReader(de441)
        val header = reader.readHeader()
        
        // J2000 = JD 2451545.0
        val j2000 = JulianDay.J2000
        
        // Should be within range
        assertThat(j2000.value).isBetween(header.startJD, header.endJD)
        
        val recordNum = reader.findRecordNumber(j2000, header)
        assertThat(recordNum).isGreaterThanOrEqualTo(0)
        
        println("J2000 is in record #$recordNum")
        println("  JD range: ${header.startJD + recordNum * header.intervalDays} - " +
               "${header.startJD + (recordNum + 1) * header.intervalDays}")
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should read data record`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val reader = JplEphemerisReader(de441)
        val header = reader.readHeader()
        
        // Read first data record
        val record = reader.readRecord(0, header)
        
        assertThat(record).isNotEmpty
        
        // First two doubles should be the time range of this segment
        val segStart = record[0]
        val segEnd = record[1]
        
        println("First data record:")
        println("  Segment start JD: $segStart")
        println("  Segment end JD: $segEnd")
        println("  Total coefficients: ${record.size}")
        
        // Segment should match header interval
        assertThat(segEnd - segStart).isCloseTo(header.intervalDays, org.assertj.core.data.Offset.offset(0.1))
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should extract coefficients for Earth at J2000`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val reader = JplEphemerisReader(de441)
        val header = reader.readHeader()
        val j2000 = JulianDay.J2000
        
        val recordNum = reader.findRecordNumber(j2000, header)
        val record = reader.readRecord(recordNum, header)
        
        val coeffs = reader.extractCoefficients(
            record,
            JplEphemerisReader.JplBody.EARTH,
            j2000,
            header
        )
        
        println("Earth coefficients at J2000:")
        println("  Sub-interval: ${coeffs.startJD} - ${coeffs.endJD}")
        println("  Num coefficients: ${coeffs.numCoefficients}")
        println("  Num intervals: ${coeffs.numIntervals}")
        println("  X coeffs[0]: ${coeffs.coefficientsX[0]}")
        println("  Y coeffs[0]: ${coeffs.coefficientsY[0]}")
        println("  Z coeffs[0]: ${coeffs.coefficientsZ[0]}")
        
        assertThat(coeffs.numCoefficients).isGreaterThan(0)
        assertThat(coeffs.coefficientsX).isNotEmpty
        assertThat(coeffs.coefficientsY).isNotEmpty
        assertThat(coeffs.coefficientsZ).isNotEmpty
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should calculate Earth position at J2000`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val engine = JplCalculationEngine(de441)
        val j2000 = JulianDay.J2000
        
        val earthPos = engine.calculatePosition(Planet.EARTH, j2000, calculateVelocity = true)
        
        println("Earth position at J2000 (barycentric):")
        println("  X: ${earthPos.position.x} km")
        println("  Y: ${earthPos.position.y} km")
        println("  Z: ${earthPos.position.z} km")
        
        val distance = sqrt(
            earthPos.position.x * earthPos.position.x +
            earthPos.position.y * earthPos.position.y +
            earthPos.position.z * earthPos.position.z
        )
        println("  Distance from SSB: $distance km")
        println("  Distance in AU: ${distance / 149597870.7}")
        
        // Earth should be roughly 1 AU from solar system barycenter
        assertThat(distance / 149597870.7).isBetween(0.98, 1.02)
        
        // Velocity should be reasonable (km/day)
        earthPos.velocity?.let { vel ->
            val speed = sqrt(vel.x * vel.x + vel.y * vel.y + vel.z * vel.z)
            println("  Speed: $speed km/day")
            println("  Speed: ${speed / 86400.0} km/s")
            
            // Earth's orbital speed should be around 30 km/s
            assertThat(speed / 86400.0).isBetween(28.0, 32.0)
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should calculate Sun position at J2000`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val engine = JplCalculationEngine(de441)
        val j2000 = JulianDay.J2000
        
        val sunPos = engine.calculateGeocentricPosition(Planet.SUN, j2000, calculateVelocity = false)
        
        println("Sun position at J2000 (geocentric):")
        println("  X: ${sunPos.position.x} km")
        println("  Y: ${sunPos.position.y} km")
        println("  Z: ${sunPos.position.z} km")
        
        val distance = sqrt(
            sunPos.position.x * sunPos.position.x +
            sunPos.position.y * sunPos.position.y +
            sunPos.position.z * sunPos.position.z
        )
        println("  Distance: $distance km")
        println("  Distance in AU: ${distance / 149597870.7}")
        
        // Earth-Sun distance should be around 1 AU
        assertThat(distance / 149597870.7).isBetween(0.98, 1.02)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "JPL_EPHE_PATH", matches = ".+")
    fun `should calculate all planets at J2000`() {
        val ephePath = System.getenv("JPL_EPHE_PATH")
        val de441 = Paths.get(ephePath, "de441.eph")
        
        if (!de441.toFile().exists()) {
            println("DE441 not found, skipping test")
            return
        }
        
        val engine = JplCalculationEngine(de441)
        val j2000 = JulianDay.J2000
        
        println("\nPlanetary positions at J2000 (geocentric):")
        println("=".repeat(60))
        
        for (planet in engine.getSupportedPlanets()) {
            if (planet == Planet.EARTH) continue  // Skip Earth (always at origin)
            
            val pos = engine.calculateGeocentricPosition(planet, j2000, calculateVelocity = false)
            
            val distance = sqrt(
                pos.position.x * pos.position.x +
                pos.position.y * pos.position.y +
                pos.position.z * pos.position.z
            )
            
            println("${planet.name.padEnd(10)}: ${String.format("%12.1f", distance)} km " +
                   "(${String.format("%.3f", distance / 149597870.7)} AU)")
        }
    }
}
