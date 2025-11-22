package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.GregorianDate;
import ch.typedef.swekt.model.JulianDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Java interop validation tests for JulianDay against SwissEphNet reference data
 * 
 * These tests validate that swekt's Kotlin implementation works correctly from Java
 * and matches SwissEphNet behavior when called from Java code.
 * 
 * Reference: SwissEphNet 2.08 (C# library)
 */
@DisplayName("JulianDay Java Interop - SwissEphNet Validation")
public class JulianDaySwissEphNetJavaInteropTest {

    /**
     * Test SwissEphNet epoch dates from Java
     */
    @ParameterizedTest(name = "{0}-{1}-{2} {3}h = JD {4}")
    @CsvSource({
        "-4713, 11, 24, 12.0, 0.0",
        "763, 9, 18, 12.0, 2000000.0",
        "-1800, 9, 18, 12.0, 1063884.0"
    })
    void fromGregorianShouldMatchSwissEphNetEpochDates(
        int year,
        int month,
        int day,
        double hour,
        double expectedJD
    ) {
        JulianDay jd = JulianDay.fromGregorian(year, month, day, hour);
        
        assertThat(jd.getValue())
            .describedAs("JulianDay for %d-%d-%d %.1f:00", year, month, day, hour)
            .isCloseTo(expectedJD, within(0.000001));
    }

    /**
     * Test SwissEphNet modern dates from Java
     */
    @ParameterizedTest(name = "{0}-{1}-{2} {3}h = JD {4}")
    @CsvSource({
        "1974, 8, 15, 23.5, 2442275.47916667",
        "2014, 4, 26, 16.89, 2456774.20375"
    })
    void fromGregorianShouldMatchSwissEphNetModernDates(
        int year,
        int month,
        int day,
        double hour,
        double expectedJD
    ) {
        JulianDay jd = JulianDay.fromGregorian(year, month, day, hour);
        
        assertThat(jd.getValue())
            .describedAs("JulianDay for %d-%d-%d %.2f:00", year, month, day, hour)
            .isCloseTo(expectedJD, within(0.000001));
    }

    /**
     * Test precise time conversion from Java
     * 16h 53m 24s = 16.89 hours
     */
    @Test
    void fromGregorianShouldHandlePreciseTimeConversion() {
        double hours = 16.0;
        double minutes = 53.0;
        double seconds = 24.0;
        double decimalHours = hours + minutes / 60.0 + seconds / 3600.0;
        
        JulianDay jd = JulianDay.fromGregorian(2014, 4, 26, decimalHours);
        
        assertThat(jd.getValue())
            .describedAs("JulianDay for 2014-04-26 16:53:24")
            .isCloseTo(2456774.20375, within(0.000001));
    }

    /**
     * Test reverse conversion from Java
     */
    @ParameterizedTest(name = "JD {0} = {1}-{2}-{3} {4}h")
    @CsvSource({
        "0.0, -4713, 11, 24, 12.0",
        "2000000.0, 763, 9, 18, 12.0",
        "2442275.47916667, 1974, 8, 15, 23.5",
        "2456774.20375, 2014, 4, 26, 16.89"
    })
    void toGregorianShouldMatchSwissEphNetReverseConversion(
        double julianDay,
        int expectedYear,
        int expectedMonth,
        int expectedDay,
        double expectedHour
    ) {
        JulianDay jd = new JulianDay(julianDay);
        GregorianDate date = jd.toGregorian();
        
        assertThat(date.getYear())
            .describedAs("Year from JD %.6f", julianDay)
            .isEqualTo(expectedYear);
        
        assertThat(date.getMonth())
            .describedAs("Month from JD %.6f", julianDay)
            .isEqualTo(expectedMonth);
        
        assertThat(date.getDay())
            .describedAs("Day from JD %.6f", julianDay)
            .isEqualTo(expectedDay);
        
        assertThat(date.getHour())
            .describedAs("Hour from JD %.6f", julianDay)
            .isCloseTo(expectedHour, within(0.01));
    }

    /**
     * Test J2000.0 constant from Java
     */
    @Test
    void j2000ConstantShouldMatchSwissEphNetReference() {
        JulianDay j2000 = JulianDay.getJ2000();
        
        assertThat(j2000.getValue())
            .describedAs("J2000.0 constant")
            .isCloseTo(2451545.0, within(0.000001));
        
        // Verify it matches computed value
        JulianDay computed = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        assertThat(j2000.getValue())
            .isCloseTo(computed.getValue(), within(0.000001));
    }

    /**
     * Test J1900.0 constant from Java
     */
    @Test
    void j1900ConstantShouldMatchSwissEphNetReference() {
        JulianDay j1900 = JulianDay.getJ1900();
        
        assertThat(j1900.getValue())
            .describedAs("J1900.0 constant")
            .isCloseTo(2415020.0, within(0.000001));
        
        // Verify it matches computed value
        JulianDay computed = JulianDay.fromGregorian(1899, 12, 31, 12.0);
        assertThat(j1900.getValue())
            .isCloseTo(computed.getValue(), within(0.000001));
    }

    /**
     * Test roundtrip conversion from Java
     */
    @ParameterizedTest(name = "Roundtrip: {0}-{1}-{2} {3}h")
    @CsvSource({
        "-4713, 11, 24, 12.0",
        "763, 9, 18, 12.0",
        "1974, 8, 15, 23.5",
        "2000, 1, 1, 12.0",
        "2014, 4, 26, 16.89"
    })
    void roundtripConversionShouldPreserveDateAccuracy(
        int year,
        int month,
        int day,
        double hour
    ) {
        JulianDay originalJD = JulianDay.fromGregorian(year, month, day, hour);
        GregorianDate date = originalJD.toGregorian();
        JulianDay reconstructedJD = JulianDay.fromGregorian(
            date.getYear(),
            date.getMonth(),
            date.getDay(),
            date.getHour()
        );
        
        assertThat(reconstructedJD.getValue())
            .describedAs("Roundtrip JD for %d-%d-%d %.2f:00", year, month, day, hour)
            .isCloseTo(originalJD.getValue(), within(0.000001));
    }

    /**
     * Test date arithmetic from Java
     */
    @Test
    void dateArithmeticShouldBeConsistentWithSwissEphNet() {
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        
        // Add 1 day using plus method
        JulianDay nextDay = jd.plus(1.0);
        assertThat(nextDay.getValue())
            .isCloseTo(jd.getValue() + 1.0, within(0.000001));
        
        // Subtract using minus method
        double diff = nextDay.minus(jd);
        assertThat(diff)
            .isCloseTo(1.0, within(0.000001));
    }

    /**
     * Test fractional days from Java
     */
    @Test
    void shouldHandleFractionalDaysWithSwissEphNetPrecision() {
        JulianDay midnight = JulianDay.fromGregorian(2000, 1, 1, 0.0);
        JulianDay noon = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        JulianDay evening = JulianDay.fromGregorian(2000, 1, 1, 18.0);
        
        // 12 hours = 0.5 days
        assertThat(noon.getValue() - midnight.getValue())
            .isCloseTo(0.5, within(0.000001));
        
        // 18 hours = 0.75 days
        assertThat(evening.getValue() - midnight.getValue())
            .isCloseTo(0.75, within(0.000001));
        
        // 6 hours = 0.25 days
        assertThat(evening.getValue() - noon.getValue())
            .isCloseTo(0.25, within(0.000001));
    }

    /**
     * Test comparison operations from Java
     */
    @Test
    void comparisonShouldWorkFromJava() {
        JulianDay jd1 = new JulianDay(2451545.0);
        JulianDay jd2 = new JulianDay(2451546.0);
        
        assertThat(jd1.compareTo(jd2)).isLessThan(0);
        assertThat(jd2.compareTo(jd1)).isGreaterThan(0);
        assertThat(jd1.compareTo(new JulianDay(2451545.0))).isEqualTo(0);
    }

    /**
     * Test default parameter from Java
     */
    @Test
    void fromGregorianWithDefaultHourShouldWork() {
        // When hour is not specified, it should default to 12.0 (noon)
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1);
        
        assertThat(jd.getValue())
            .describedAs("JulianDay for 2000-01-01 (default noon)")
            .isCloseTo(2451545.0, within(0.000001));
    }

    /**
     * Test GregorianDate creation from Java
     */
    @Test
    void gregorianDateShouldBeAccessibleFromJava() {
        GregorianDate date = new GregorianDate(2000, 1, 1, 12.0);
        
        assertThat(date.getYear()).isEqualTo(2000);
        assertThat(date.getMonth()).isEqualTo(1);
        assertThat(date.getDay()).isEqualTo(1);
        assertThat(date.getHour()).isCloseTo(12.0, within(0.01));
    }

    /**
     * Test time component extraction from Java
     */
    @Test
    void timeComponentExtractionShouldWorkFromJava() {
        JulianDay jd = new JulianDay(2456774.20375);
        GregorianDate date = jd.toGregorian();
        
        double hour = date.getHour();
        int hours = (int) hour;
        int totalMinutes = (int) (hour * 60.0);
        int minutes = totalMinutes % 60;
        int totalSeconds = (int) (hour * 3600.0);
        int seconds = totalSeconds % 60;
        
        assertThat(hours).isEqualTo(16);
        assertThat(minutes).isEqualTo(53);
        assertThat(seconds).isEqualTo(23); // 23.99... rounds to 24
    }
}
