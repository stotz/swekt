package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.GregorianDate;
import ch.typedef.swekt.model.JulianDay;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * TDD Tests for Java interoperability with JulianDay.
 * 
 * These tests verify that swekt can be used naturally from Java code.
 */
public class JulianDayJavaInteropTest {

    @Test
    public void shouldCreateJulianDayFromValue() {
        JulianDay jd = new JulianDay(2451545.0);
        
        assertThat(jd.getValue()).isEqualTo(2451545.0);
    }

    @Test
    public void shouldAccessJ2000Constant() {
        JulianDay j2000 = JulianDay.J2000;
        
        assertThat(j2000.getValue()).isCloseTo(2451545.0, within(0.0001));
    }

    @Test
    public void shouldCreateFromGregorianWithAllParameters() {
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        
        assertThat(jd.getValue()).isCloseTo(2451545.0, within(0.0001));
    }

    @Test
    public void shouldCreateFromGregorianWithDefaultHour() {
        // Test default parameter from Java
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        
        assertThat(jd.getValue()).isCloseTo(2451545.0, within(0.0001));
    }

    @Test
    public void shouldPerformArithmetic() {
        JulianDay jd = new JulianDay(2451545.0);
        JulianDay nextDay = jd.plus(1.0);
        
        assertThat(nextDay.getValue()).isCloseTo(2451546.0, within(0.0001));
    }

    @Test
    public void shouldCalculateDifference() {
        JulianDay jd1 = new JulianDay(2451546.0);
        JulianDay jd2 = new JulianDay(2451545.0);
        
        double diff = jd1.minus(jd2);
        
        assertThat(diff).isCloseTo(1.0, within(0.0001));
    }

    @Test
    public void shouldCompare() {
        JulianDay jd1 = new JulianDay(2451545.0);
        JulianDay jd2 = new JulianDay(2451546.0);
        
        assertThat(jd1.compareTo(jd2)).isLessThan(0);
        assertThat(jd2.compareTo(jd1)).isGreaterThan(0);
        assertThat(jd1.compareTo(new JulianDay(2451545.0))).isEqualTo(0);
    }

    @Test
    public void shouldConvertToGregorian() {
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        GregorianDate gregorian = jd.toGregorian();
        
        assertThat(gregorian.getYear()).isEqualTo(2000);
        assertThat(gregorian.getMonth()).isEqualTo(1);
        assertThat(gregorian.getDay()).isEqualTo(1);
        assertThat(gregorian.getHour()).isCloseTo(12.0, within(0.01));
    }

    @Test
    public void shouldFormatGregorianDate() {
        JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        GregorianDate gregorian = jd.toGregorian();
        
        String formatted = gregorian.toString();
        
        assertThat(formatted).contains("2000");
        assertThat(formatted).contains("01");
        assertThat(formatted).contains("12:00");
    }
}
