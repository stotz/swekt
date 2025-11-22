package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.time.SiderealTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Java interoperability tests for SiderealTime.
 */
@DisplayName("SiderealTime Java Interop Tests")
public class SiderealTimeJavaInteropTest {

    @Test
    @DisplayName("Calculate GMST from Java")
    public void testCalculateGMST() {
        JulianDay jd = JulianDay.getJ2000();
        
        double gmst = SiderealTime.calculateGMST(jd);
        
        assertThat(gmst).isBetween(0.0, 24.0);
        assertThat(gmst).isCloseTo(18.697, within(0.01));
    }

    @Test
    @DisplayName("Calculate GAST from Java")
    public void testCalculateGAST() {
        JulianDay jd = JulianDay.getJ2000();
        
        double gast = SiderealTime.calculateGAST(jd);
        
        assertThat(gast).isBetween(0.0, 24.0);
    }

    @Test
    @DisplayName("Calculate LST from Java")
    public void testCalculateLST() {
        JulianDay jd = JulianDay.getJ2000();
        double longitude = 15.0; // 15 degrees East
        
        double lst = SiderealTime.calculateLST(jd, longitude);
        
        assertThat(lst).isBetween(0.0, 24.0);
    }

    @Test
    @DisplayName("Convert hours to degrees from Java")
    public void testHoursToDegrees() {
        assertThat(SiderealTime.hoursToDegrees(0.0)).isEqualTo(0.0);
        assertThat(SiderealTime.hoursToDegrees(6.0)).isEqualTo(90.0);
        assertThat(SiderealTime.hoursToDegrees(12.0)).isEqualTo(180.0);
        assertThat(SiderealTime.hoursToDegrees(24.0)).isEqualTo(360.0);
    }

    @Test
    @DisplayName("Convert degrees to hours from Java")
    public void testDegreesToHours() {
        assertThat(SiderealTime.degreesToHours(0.0)).isEqualTo(0.0);
        assertThat(SiderealTime.degreesToHours(90.0)).isEqualTo(6.0);
        assertThat(SiderealTime.degreesToHours(180.0)).isEqualTo(12.0);
        assertThat(SiderealTime.degreesToHours(360.0)).isEqualTo(24.0);
    }

    @Test
    @DisplayName("Convert HMS to decimal hours from Java")
    public void testHMSToHours() {
        double hours = SiderealTime.hmsToHours(18, 41, 50.55);
        assertThat(hours).isCloseTo(18.6973, within(0.0001));
    }

    @Test
    @DisplayName("LST at Greenwich equals GMST")
    public void testLSTAtGreenwich() {
        JulianDay jd = JulianDay.getJ2000();
        double longitude = 0.0; // Greenwich
        
        double lst = SiderealTime.calculateLST(jd, longitude);
        double gmst = SiderealTime.calculateGMST(jd);
        
        assertThat(lst).isCloseTo(gmst, within(0.001));
    }

    @Test
    @DisplayName("GMST increases daily")
    public void testGMSTIncreases() {
        JulianDay day1 = JulianDay.fromGregorian(2000, 1, 1, 0.0);
        JulianDay day2 = JulianDay.fromGregorian(2000, 1, 2, 0.0);
        
        double gmst1 = SiderealTime.calculateGMST(day1);
        double gmst2 = SiderealTime.calculateGMST(day2);
        
        double diff = gmst2 - gmst1;
        if (diff < 0) diff += 24.0;
        
        // GMST increases by ~24.066 hours per solar day, but after
        // normalization to 0-24h, this appears as ~0.066 hours
        assertThat(diff).isCloseTo(0.066, within(0.01));
    }
}
