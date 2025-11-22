package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlin.math.abs

@DisplayName("TimeConversion Tests")
class TimeConversionTest {

    @Test
    @DisplayName("UT to TT conversion adds Delta T")
    fun testUTtoTT() {
        val utJd = JulianDay.J2000
        val ttJd = TimeConversion.utToTT(utJd)
        
        val deltaT = DeltaT.calculate(utJd)
        
        // TT should be UT + Delta T
        assertThat(ttJd.value).isCloseTo(utJd.value + deltaT, org.assertj.core.data.Offset.offset(1e-10))
    }

    @Test
    @DisplayName("TT to UT conversion subtracts Delta T")
    fun testTTtoUT() {
        val ttJd = JulianDay.J2000
        val utJd = TimeConversion.ttToUT(ttJd)
        
        // Converting back should give approximately the original value
        val ttJdBack = TimeConversion.utToTT(utJd)
        
        assertThat(ttJdBack.value).isCloseTo(ttJd.value, org.assertj.core.data.Offset.offset(1e-6))
    }

    @Test
    @DisplayName("UT → TT → UT round trip is accurate")
    fun testUTtoTTtoUT() {
        val originalUT = JulianDay.fromGregorian(2020, 6, 15, 12.0)
        
        val tt = TimeConversion.utToTT(originalUT)
        val backToUT = TimeConversion.ttToUT(tt)
        
        // Should recover original UT within 0.001 seconds
        assertThat(backToUT.value).isCloseTo(originalUT.value, org.assertj.core.data.Offset.offset(1e-8))
    }

    @Test
    @DisplayName("UTC to TT conversion works for modern dates")
    fun testUTCtoTT() {
        val utcJd = JulianDay.fromGregorian(2020, 1, 1, 0.0)
        val ttJd = TimeConversion.utcToTT(utcJd)
        
        // In 2020, Delta T ~ 69 seconds ~ 0.0008 days
        val difference = (ttJd.value - utcJd.value) * 86400.0 // Convert to seconds
        
        assertThat(difference).isBetween(68.0, 71.0)
    }

    @Test
    @DisplayName("TT to TDB conversion applies periodic correction")
    fun testTTtoTDB() {
        val ttJd = JulianDay.J2000
        val tdbJd = TimeConversion.ttToTDB(ttJd)
        
        // TDB - TT should be small (max ~2 milliseconds = ~2e-5 days)
        val difference = abs(tdbJd.value - ttJd.value)
        
        assertThat(difference).isLessThan(3e-5) // 3 milliseconds tolerance
    }

    @Test
    @DisplayName("TDB to TT round trip is accurate")
    fun testTDBtoTTtoTDB() {
        val originalTDB = JulianDay.fromGregorian(2020, 6, 15, 12.0)
        
        val tt = TimeConversion.tdbToTT(originalTDB)
        val backToTDB = TimeConversion.ttToTDB(tt)
        
        // Should recover original TDB within microseconds
        assertThat(backToTDB.value).isCloseTo(originalTDB.value, org.assertj.core.data.Offset.offset(1e-10))
    }

    @Test
    @DisplayName("UT to TDB conversion combines Delta T and periodic correction")
    fun testUTtoTDB() {
        val utJd = JulianDay.J2000
        val tdbJd = TimeConversion.utToTDB(utJd)
        
        // Manual calculation: UT → TT → TDB
        val ttJd = TimeConversion.utToTT(utJd)
        val tdbJdManual = TimeConversion.ttToTDB(ttJd)
        
        assertThat(tdbJd.value).isCloseTo(tdbJdManual.value, org.assertj.core.data.Offset.offset(1e-10))
    }

    @Test
    @DisplayName("TDB to UT round trip is accurate")
    fun testTDBtoUTtoTDB() {
        val originalTDB = JulianDay.fromGregorian(2020, 6, 15, 12.0)
        
        val ut = TimeConversion.tdbToUT(originalTDB)
        val backToTDB = TimeConversion.utToTDB(ut)
        
        // Should recover original TDB within 0.001 seconds
        assertThat(backToTDB.value).isCloseTo(originalTDB.value, org.assertj.core.data.Offset.offset(1e-8))
    }

    @Test
    @DisplayName("getDeltaT returns same value as DeltaT.calculate")
    fun testGetDeltaT() {
        val jd = JulianDay.J2000
        
        val dt1 = TimeConversion.getDeltaT(jd)
        val dt2 = DeltaT.calculate(jd)
        
        assertThat(dt1).isEqualTo(dt2)
    }

    @Test
    @DisplayName("getDeltaTSeconds returns value in seconds")
    fun testGetDeltaTSeconds() {
        val jd = JulianDay.J2000
        
        val dtDays = TimeConversion.getDeltaT(jd)
        val dtSeconds = TimeConversion.getDeltaTSeconds(jd)
        
        // Should be the same value in different units
        assertThat(dtDays * 86400.0).isCloseTo(dtSeconds, org.assertj.core.data.Offset.offset(0.001))
    }

    @Test
    @DisplayName("Time conversions work for historical dates")
    fun testHistoricalDates() {
        val jd1800 = JulianDay.fromGregorian(1800, 1, 1, 0.0)
        
        val tt = TimeConversion.utToTT(jd1800)
        val backToUT = TimeConversion.ttToUT(tt)
        
        // Should be accurate even for historical dates
        assertThat(backToUT.value).isCloseTo(jd1800.value, org.assertj.core.data.Offset.offset(1e-6))
    }

    @Test
    @DisplayName("Time conversions work for ancient dates")
    fun testAncientDates() {
        // 500 BCE
        val jdAncient = JulianDay.fromGregorian(-499, 1, 1, 0.0)
        
        val tt = TimeConversion.utToTT(jdAncient)
        val backToUT = TimeConversion.ttToUT(tt)
        
        // Should be accurate even for ancient dates
        assertThat(backToUT.value).isCloseTo(jdAncient.value, org.assertj.core.data.Offset.offset(1e-6))
    }

    @Test
    @DisplayName("TT is always greater than UT for modern dates")
    fun testTTGreaterThanUT() {
        val utJd = JulianDay.fromGregorian(2020, 1, 1, 0.0)
        val ttJd = TimeConversion.utToTT(utJd)
        
        // TT = UT + Delta T, and Delta T is positive in modern era
        assertThat(ttJd.value).isGreaterThan(utJd.value)
    }

    @Test
    @DisplayName("Time scale conversions preserve precision")
    fun testPrecisionPreservation() {
        val originalUT = JulianDay(2451545.5) // J2000 + 0.5 days
        
        // UT → TT → TDB → TT → UT
        val tt1 = TimeConversion.utToTT(originalUT)
        val tdb = TimeConversion.ttToTDB(tt1)
        val tt2 = TimeConversion.tdbToTT(tdb)
        val finalUT = TimeConversion.ttToUT(tt2)
        
        // Should preserve precision through multiple conversions
        assertThat(finalUT.value).isCloseTo(originalUT.value, org.assertj.core.data.Offset.offset(1e-8))
    }
}
