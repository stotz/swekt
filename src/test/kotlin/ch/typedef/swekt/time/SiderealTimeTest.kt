package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.math.abs

/**
 * Tests for Sidereal Time calculations.
 *
 * Sidereal time is the hour angle of the vernal equinox,
 * measuring the Earth's rotation relative to the stars rather than the Sun.
 *
 * Reference values from:
 * - U.S. Naval Observatory Astronomical Almanac
 * - Meeus, "Astronomical Algorithms" (1998), Chapter 12
 * - Swiss Ephemeris swe_sidtime() function
 */
@DisplayName("SiderealTime Tests")
class SiderealTimeTest {

    @Test
    @DisplayName("Calculate GMST at J2000 epoch")
    fun testGMSTAtJ2000() {
        // At J2000.0 (2000-01-01 12:00:00 TT), GMST0 should be 18h 41m 50.55s
        // = 18.697375 hours
        val j2000 = JulianDay.J2000
        
        val gmst = SiderealTime.calculateGMST(j2000)
        
        // Allow 1 second tolerance (0.000278 hours)
        assertThat(gmst).isCloseTo(18.697375, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Calculate GMST at midnight")
    fun testGMSTAtMidnight() {
        // 2000-01-01 00:00:00 UT
        // Expected GMST0: 6h 39m 51.25s = 6.664236 hours
        val jd = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        
        val gmst = SiderealTime.calculateGMST(jd)
        
        assertThat(gmst).isCloseTo(6.664, org.assertj.core.data.Offset.offset(0.01))
    }

    @Test
    @DisplayName("Calculate GMST for arbitrary date")
    fun testGMSTArbitraryDate() {
        // 1987-04-10 00:00:00 UT
        // From Meeus example: GMST = 13h 10m 46.37s = 13.179547 hours
        val jd = JulianDay.fromGregorian(1987, 4, 10, 0.0)
        
        val gmst = SiderealTime.calculateGMST(jd)
        
        assertThat(gmst).isCloseTo(13.179547, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Calculate GMST with fractional day")
    fun testGMSTWithTime() {
        // 1987-04-10 19:21:00 UT = JD 2446895.30625
        // From Meeus: GMST = 8h 34m 57.09s = 8.582525 hours
        val jd = JulianDay.fromGregorian(1987, 4, 10, 19.35) // 19:21:00
        
        val gmst = SiderealTime.calculateGMST(jd)
        
        assertThat(gmst).isCloseTo(8.582525, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("GMST should always be in range 0-24 hours")
    fun testGMSTRange() {
        // Test various dates
        val testDates = listOf(
            JulianDay.fromGregorian(1900, 1, 1, 0.0),
            JulianDay.fromGregorian(2000, 6, 15, 12.5),
            JulianDay.fromGregorian(2050, 12, 31, 23.99),
            JulianDay.fromGregorian(1600, 3, 10, 6.0)
        )
        
        testDates.forEach { jd ->
            val gmst = SiderealTime.calculateGMST(jd)
            assertThat(gmst).isBetween(0.0, 24.0)
        }
    }

    @Test
    @DisplayName("Calculate GAST (apparent sidereal time) at J2000")
    fun testGASTAtJ2000() {
        val j2000 = JulianDay.J2000
        
        val gast = SiderealTime.calculateGAST(j2000)
        
        // GAST differs from GMST by the equation of equinoxes
        // Should be close to GMST but not identical
        assertThat(gast).isNotEqualTo(SiderealTime.calculateGMST(j2000))
        assertThat(gast).isBetween(0.0, 24.0)
    }

    @Test
    @DisplayName("Calculate Local Sidereal Time at longitude 0")
    fun testLSTAtLongitudeZero() {
        val jd = JulianDay.J2000
        val longitude = 0.0 // Greenwich
        
        val lst = SiderealTime.calculateLST(jd, longitude)
        
        // At Greenwich, LST = GMST
        val gmst = SiderealTime.calculateGMST(jd)
        assertThat(lst).isCloseTo(gmst, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Calculate Local Sidereal Time at positive longitude")
    fun testLSTAtPositiveLongitude() {
        val jd = JulianDay.J2000
        val longitude = 15.0 // 15° East = 1 hour ahead
        
        val lst = SiderealTime.calculateLST(jd, longitude)
        val gmst = SiderealTime.calculateGMST(jd)
        
        // LST should be 1 hour ahead of GMST
        val expected = (gmst + 1.0) % 24.0
        assertThat(lst).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Calculate Local Sidereal Time at negative longitude")
    fun testLSTAtNegativeLongitude() {
        val jd = JulianDay.J2000
        val longitude = -75.0 // 75° West = 5 hours behind
        
        val lst = SiderealTime.calculateLST(jd, longitude)
        val gmst = SiderealTime.calculateGMST(jd)
        
        // LST should be 5 hours behind GMST
        var expected = gmst - 5.0
        if (expected < 0.0) expected += 24.0
        assertThat(lst).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("GMST increases by ~1 sidereal day per solar day")
    fun testGMSTDailyIncrease() {
        val jd1 = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        val jd2 = JulianDay.fromGregorian(2000, 1, 2, 0.0)
        
        val gmst1 = SiderealTime.calculateGMST(jd1)
        val gmst2 = SiderealTime.calculateGMST(jd2)
        
        // Sidereal day is ~3m 56s shorter than solar day
        // So GMST increases by ~24h 3m 56s ≈ 24.066 hours per solar day
        // After normalization to 0-24h, this appears as ~0.066 hours
        var diff = gmst2 - gmst1
        if (diff < 0) diff += 24.0
        
        assertThat(diff).isCloseTo(0.066, org.assertj.core.data.Offset.offset(0.01))
    }

    @Test
    @DisplayName("Convert sidereal hours to degrees")
    fun testHoursToDegrees() {
        assertThat(SiderealTime.hoursToDegrees(0.0)).isEqualTo(0.0)
        assertThat(SiderealTime.hoursToDegrees(6.0)).isEqualTo(90.0)
        assertThat(SiderealTime.hoursToDegrees(12.0)).isEqualTo(180.0)
        assertThat(SiderealTime.hoursToDegrees(18.0)).isEqualTo(270.0)
        assertThat(SiderealTime.hoursToDegrees(24.0)).isEqualTo(360.0)
    }

    @Test
    @DisplayName("Convert degrees to sidereal hours")
    fun testDegreesToHours() {
        assertThat(SiderealTime.degreesToHours(0.0)).isEqualTo(0.0)
        assertThat(SiderealTime.degreesToHours(90.0)).isEqualTo(6.0)
        assertThat(SiderealTime.degreesToHours(180.0)).isEqualTo(12.0)
        assertThat(SiderealTime.degreesToHours(270.0)).isEqualTo(18.0)
        assertThat(SiderealTime.degreesToHours(360.0)).isEqualTo(24.0)
    }

    @Test
    @DisplayName("GMST0 at J2000 matches reference")
    fun testGMST0AtJ2000() {
        // GMST at 0h UT on 2000-01-01 should be 6h 39m 51.25s
        val jd = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        val gmst0 = SiderealTime.calculateGMST0(jd)
        
        assertThat(gmst0).isCloseTo(6.664236, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Test GMST calculation consistency")
    fun testGMSTConsistency() {
        // GMST should be continuous - no jumps
        val jd1 = JulianDay(2451545.0)
        val jd2 = JulianDay(2451545.1) // 2.4 hours later
        
        val gmst1 = SiderealTime.calculateGMST(jd1)
        val gmst2 = SiderealTime.calculateGMST(jd2)
        
        // Difference should be approximately 2.4 hours (0.1 days = 2.4 hours solar)
        var diff = gmst2 - gmst1
        if (diff < 0) diff += 24.0
        
        // 0.1 solar days ≈ 0.1002738 sidereal days ≈ 2.4066 sidereal hours
        // (1 solar day = 1.002737909 sidereal days)
        assertThat(diff).isCloseTo(2.4066, org.assertj.core.data.Offset.offset(0.01))
    }
}
