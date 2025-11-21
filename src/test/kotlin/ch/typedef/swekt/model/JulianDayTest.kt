package ch.typedef.swekt.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * TDD Tests for JulianDay value class
 */
class JulianDayTest {

    @Test
    fun `should create JulianDay from value`() {
        val jd = JulianDay(2451545.0)
        
        assertThat(jd.value).isEqualTo(2451545.0)
    }

    @Test
    fun `should create JulianDay from Gregorian date`() {
        // J2000.0 = 2000-01-01 12:00:00 UT
        val jd = JulianDay.fromGregorian(2000, 1, 1, 12.0)
        
        assertThat(jd.value).isCloseTo(2451545.0, within(0.0001))
    }

    @Test
    fun `should create JulianDay from Gregorian date with default noon`() {
        val jd = JulianDay.fromGregorian(2000, 1, 1)
        
        assertThat(jd.value).isCloseTo(2451545.0, within(0.0001))
    }

    @Test
    fun `should handle different times of day`() {
        val midnight = JulianDay.fromGregorian(2000, 1, 1, 0.0)
        val noon = JulianDay.fromGregorian(2000, 1, 1, 12.0)
        val evening = JulianDay.fromGregorian(2000, 1, 1, 18.0)
        
        assertThat(noon.value - midnight.value).isCloseTo(0.5, within(0.0001))
        assertThat(evening.value - midnight.value).isCloseTo(0.75, within(0.0001))
    }

    @Test
    fun `should reject invalid month`() {
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 13, 1)
        }
        
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 0, 1)
        }
    }

    @Test
    fun `should reject invalid day`() {
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 1, 32)
        }
        
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 1, 0)
        }
    }

    @Test
    fun `should reject invalid hour`() {
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 1, 1, 24.0)
        }
        
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2000, 1, 1, -1.0)
        }
    }

    @Test
    fun `should handle leap year correctly`() {
        val feb29_2000 = JulianDay.fromGregorian(2000, 2, 29) // Leap year
        val mar01_2000 = JulianDay.fromGregorian(2000, 3, 1)
        
        assertThat(mar01_2000.value - feb29_2000.value).isCloseTo(1.0, within(0.0001))
    }

    @Test
    fun `should reject Feb 29 in non-leap year`() {
        assertThrows<IllegalArgumentException> {
            JulianDay.fromGregorian(2001, 2, 29)
        }
    }

    @Test
    fun `should convert back to Gregorian`() {
        val original = GregorianDate(2000, 1, 1, 12.0)
        val jd = JulianDay.fromGregorian(original.year, original.month, original.day, original.hour)
        val converted = jd.toGregorian()
        
        assertThat(converted.year).isEqualTo(original.year)
        assertThat(converted.month).isEqualTo(original.month)
        assertThat(converted.day).isEqualTo(original.day)
        assertThat(converted.hour).isCloseTo(original.hour, within(0.01))
    }

    @Test
    fun `should handle historical dates before Common Era`() {
        // Example: -500-01-01 (500 BCE)
        // Julian Day 0 = 4713 BCE, so 500 BCE is much later and has positive JD
        val jd = JulianDay.fromGregorian(-500, 1, 1)
        
        assertThat(jd.value).isGreaterThan(0.0)
        
        // Should be able to convert back
        val gregorian = jd.toGregorian()
        assertThat(gregorian.year).isEqualTo(-500)
    }

    @Test
    fun `should support addition`() {
        val jd = JulianDay(2451545.0)
        val nextDay = jd + 1.0
        
        assertThat(nextDay.value).isCloseTo(2451546.0, within(0.0001))
    }

    @Test
    fun `should support subtraction`() {
        val jd1 = JulianDay(2451546.0)
        val jd2 = JulianDay(2451545.0)
        
        val diff = jd1 - jd2
        
        assertThat(diff).isCloseTo(1.0, within(0.0001))
    }

    @Test
    fun `should be comparable`() {
        val jd1 = JulianDay(2451545.0)
        val jd2 = JulianDay(2451546.0)
        
        assertThat(jd1 < jd2).isTrue()
        assertThat(jd2 > jd1).isTrue()
        assertThat(jd1 == JulianDay(2451545.0)).isTrue()
    }
}
