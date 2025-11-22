package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.math.abs

@DisplayName("DeltaT Tests")
class DeltaTTest {

    @Test
    @DisplayName("Delta T at J2000 should be approximately 64 seconds")
    fun testDeltaTAtJ2000() {
        val j2000 = JulianDay.J2000
        val deltaT = DeltaT.calculateSeconds(j2000.value)
        
        // At J2000 (2000-01-01), Delta T was approximately 63.8 seconds
        assertThat(deltaT).isBetween(63.0, 65.0)
    }

    @Test
    @DisplayName("Delta T in 2020 should be approximately 69-70 seconds")
    fun testDeltaTIn2020() {
        // 2020-01-01 00:00:00 UTC
        val jd2020 = JulianDay.fromGregorian(2020, 1, 1, 0.0)
        val deltaT = DeltaT.calculateSeconds(jd2020.value)
        
        // In 2020, Delta T was approximately 69 seconds
        assertThat(deltaT).isBetween(68.0, 71.0)
    }

    @Test
    @DisplayName("Delta T in 1900 should be approximately -3 seconds")
    fun testDeltaTIn1900() {
        val jd1900 = JulianDay.fromGregorian(1900, 1, 1, 0.0)
        val deltaT = DeltaT.calculateSeconds(jd1900.value)
        
        // In 1900, Delta T was approximately -2.79 seconds
        assertThat(deltaT).isBetween(-5.0, 0.0)
    }

    @Test
    @DisplayName("Delta T in 1800 should be approximately 14 seconds")
    fun testDeltaTIn1800() {
        val jd1800 = JulianDay.fromGregorian(1800, 1, 1, 0.0)
        val deltaT = DeltaT.calculateSeconds(jd1800.value)
        
        // In 1800, Delta T was approximately 13.72 seconds
        assertThat(deltaT).isBetween(12.0, 16.0)
    }

    @Test
    @DisplayName("Delta T increases over time (modern era)")
    fun testDeltaTIncreases() {
        val jd1990 = JulianDay.fromGregorian(1990, 1, 1, 0.0)
        val jd2000 = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        val jd2010 = JulianDay.fromGregorian(2010, 1, 1, 0.0)
        val jd2020 = JulianDay.fromGregorian(2020, 1, 1, 0.0)
        
        val dt1990 = DeltaT.calculateSeconds(jd1990.value)
        val dt2000 = DeltaT.calculateSeconds(jd2000.value)
        val dt2010 = DeltaT.calculateSeconds(jd2010.value)
        val dt2020 = DeltaT.calculateSeconds(jd2020.value)
        
        // Delta T should increase over time in modern era
        assertThat(dt2000).isGreaterThan(dt1990)
        assertThat(dt2010).isGreaterThan(dt2000)
        assertThat(dt2020).isGreaterThan(dt2010)
    }

    @Test
    @DisplayName("Delta T for ancient dates uses parabolic extrapolation")
    fun testAncientDeltaT() {
        // 500 BCE = -499 in astronomical year numbering
        val jdAncient = JulianDay.fromGregorian(-499, 1, 1, 0.0)
        val deltaT = DeltaT.calculateSeconds(jdAncient.value)
        
        // Ancient Delta T should be large and positive (Earth rotated faster)
        // For 500 BCE, Delta T should be approximately 17,000 seconds (~5 hours)
        assertThat(deltaT).isGreaterThan(10000.0)
    }

    @Test
    @DisplayName("Calculate method returns Delta T in days")
    fun testCalculateInDays() {
        val j2000 = JulianDay.J2000
        val deltaTDays = DeltaT.calculate(j2000)
        val deltaTSeconds = DeltaT.calculateSeconds(j2000.value)
        
        // Should be the same value in different units
        assertThat(deltaTDays * 86400.0).isCloseTo(deltaTSeconds, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Leap seconds are counted correctly for modern dates")
    fun testLeapSeconds() {
        // Before first leap second (1972-01-01)
        val jd1971 = JulianDay.fromGregorian(1971, 12, 31, 0.0)
        val dt1971 = DeltaT.calculateSeconds(jd1971.value)
        
        // After first leap second (1972-01-01)
        val jd1972 = JulianDay.fromGregorian(1972, 1, 2, 0.0)
        val dt1972 = DeltaT.calculateSeconds(jd1972.value)
        
        // After leap second (2017-01-01) - 37 leap seconds total
        val jd2017 = JulianDay.fromGregorian(2017, 1, 2, 0.0)
        val dt2017 = DeltaT.calculateSeconds(jd2017.value)
        
        // Modern Delta T should be: leap_seconds + 32.184
        // 2017: 37 + 32.184 = 69.184 seconds
        assertThat(dt2017).isCloseTo(69.184, org.assertj.core.data.Offset.offset(1.0))
    }

    @Test
    @DisplayName("Year to JD conversion is consistent")
    fun testYearToJdConversion() {
        val year = 2000.0
        val jd = DeltaT.yearToJd(year)
        
        // 2000.0 should correspond to J2000
        assertThat(jd).isCloseTo(JulianDay.J2000.value, org.assertj.core.data.Offset.offset(1.0))
    }

    @Test
    @DisplayName("Delta T is smooth and continuous")
    fun testDeltaTContinuity() {
        // Test that Delta T doesn't have discontinuities
        val jd = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        
        val dt1 = DeltaT.calculateSeconds(jd.value)
        val dt2 = DeltaT.calculateSeconds(jd.value + 1.0) // Next day
        val dt3 = DeltaT.calculateSeconds(jd.value + 2.0) // Day after
        
        // Delta T should change smoothly (less than 0.1 seconds per day)
        assertThat(abs(dt2 - dt1)).isLessThan(0.1)
        assertThat(abs(dt3 - dt2)).isLessThan(0.1)
    }

    @Test
    @DisplayName("Delta T matches known historical values within tolerance")
    fun testHistoricalValues() {
        // Test against known Delta T values from literature
        
        // 1950: ~29 seconds
        val jd1950 = JulianDay.fromGregorian(1950, 1, 1, 0.0)
        assertThat(DeltaT.calculateSeconds(jd1950.value)).isBetween(28.0, 30.0)
        
        // 1975: ~45 seconds
        val jd1975 = JulianDay.fromGregorian(1975, 1, 1, 0.0)
        assertThat(DeltaT.calculateSeconds(jd1975.value)).isBetween(44.0, 47.0)
        
        // 2005: ~65 seconds
        val jd2005 = JulianDay.fromGregorian(2005, 1, 1, 0.0)
        assertThat(DeltaT.calculateSeconds(jd2005.value)).isBetween(64.0, 66.0)
    }
}
