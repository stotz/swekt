package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.time.DeltaT;
import ch.typedef.swekt.time.TimeConversion;
import ch.typedef.swekt.time.TimeScale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

@DisplayName("Time Systems Java Interop Tests")
public class TimeSystemsJavaInteropTest {

    @Test
    @DisplayName("DeltaT can be called from Java")
    public void testDeltaTFromJava() {
        JulianDay j2000 = JulianDay.J2000;
        double deltaT = DeltaT.calculateSeconds(j2000.getValue());
        
        // At J2000, Delta T was approximately 63.8 seconds
        assertThat(deltaT).isBetween(63.0, 65.0);
    }

    @Test
    @DisplayName("DeltaT calculate method works from Java")
    public void testDeltaTCalculateFromJava() {
        JulianDay j2000 = JulianDay.J2000;
        double deltaTDays = DeltaT.calculate(j2000);
        double deltaTSeconds = DeltaT.calculateSeconds(j2000.getValue());
        
        // Should be the same value in different units
        assertThat(deltaTDays * 86400.0).isCloseTo(deltaTSeconds, offset(0.001));
    }

    @Test
    @DisplayName("TimeConversion.utToTT works from Java")
    public void testUTtoTTfromJava() {
        JulianDay utJd = JulianDay.J2000;
        JulianDay ttJd = TimeConversion.utToTT(utJd);
        
        double deltaT = DeltaT.calculate(utJd);
        
        // TT should be UT + Delta T
        assertThat(ttJd.getValue()).isCloseTo(utJd.getValue() + deltaT, offset(1e-10));
    }

    @Test
    @DisplayName("TimeConversion.ttToUT works from Java")
    public void testTTtoUTfromJava() {
        JulianDay ttJd = JulianDay.J2000;
        JulianDay utJd = TimeConversion.ttToUT(ttJd);
        
        // Converting back should give approximately the original value
        JulianDay ttJdBack = TimeConversion.utToTT(utJd);
        
        assertThat(ttJdBack.getValue()).isCloseTo(ttJd.getValue(), offset(1e-6));
    }

    @Test
    @DisplayName("TimeConversion.utcToTT works from Java")
    public void testUTCtoTTfromJava() {
        JulianDay utcJd = JulianDay.fromGregorian(2020, 1, 1, 0.0);
        JulianDay ttJd = TimeConversion.utcToTT(utcJd);
        
        // In 2020, Delta T ~ 69 seconds ~ 0.0008 days
        double difference = (ttJd.getValue() - utcJd.getValue()) * 86400.0; // Convert to seconds
        
        assertThat(difference).isBetween(68.0, 71.0);
    }

    @Test
    @DisplayName("TimeConversion.ttToTDB works from Java")
    public void testTTtoTDBfromJava() {
        JulianDay ttJd = JulianDay.J2000;
        JulianDay tdbJd = TimeConversion.ttToTDB(ttJd);
        
        // TDB - TT should be small (max ~2 milliseconds = ~2e-5 days)
        double difference = Math.abs(tdbJd.getValue() - ttJd.getValue());
        
        assertThat(difference).isLessThan(3e-5); // 3 milliseconds tolerance
    }

    @Test
    @DisplayName("TimeConversion.tdbToTT works from Java")
    public void testTDBtoTTfromJava() {
        JulianDay tdbJd = JulianDay.fromGregorian(2020, 6, 15, 12.0);
        
        JulianDay tt = TimeConversion.tdbToTT(tdbJd);
        JulianDay backToTDB = TimeConversion.ttToTDB(tt);
        
        // Should recover original TDB
        assertThat(backToTDB.getValue()).isCloseTo(tdbJd.getValue(), offset(1e-10));
    }

    @Test
    @DisplayName("TimeConversion.getDeltaT works from Java")
    public void testGetDeltaTFromJava() {
        JulianDay jd = JulianDay.J2000;
        
        double dt1 = TimeConversion.getDeltaT(jd);
        double dt2 = DeltaT.calculate(jd);
        
        assertThat(dt1).isEqualTo(dt2);
    }

    @Test
    @DisplayName("TimeConversion.getDeltaTSeconds works from Java")
    public void testGetDeltaTSecondsFromJava() {
        JulianDay jd = JulianDay.J2000;
        
        double dtDays = TimeConversion.getDeltaT(jd);
        double dtSeconds = TimeConversion.getDeltaTSeconds(jd);
        
        // Should be the same value in different units
        assertThat(dtDays * 86400.0).isCloseTo(dtSeconds, offset(0.001));
    }

    @Test
    @DisplayName("TimeScale enum is accessible from Java")
    public void testTimeScaleFromJava() {
        TimeScale ut1 = TimeScale.UT1;
        TimeScale utc = TimeScale.UTC;
        TimeScale tt = TimeScale.TT;
        TimeScale tdb = TimeScale.TDB;
        TimeScale tai = TimeScale.TAI;
        
        assertThat(ut1).isNotNull();
        assertThat(utc).isNotNull();
        assertThat(tt).isNotNull();
        assertThat(tdb).isNotNull();
        assertThat(tai).isNotNull();
    }

    @Test
    @DisplayName("TimeScale.getDefault works from Java")
    public void testTimeScaleDefaultFromJava() {
        TimeScale defaultScale = TimeScale.getDefault();
        
        assertThat(defaultScale).isEqualTo(TimeScale.TT);
    }

    @Test
    @DisplayName("Complete time conversion workflow from Java")
    public void testCompleteWorkflowFromJava() {
        // User provides UTC time (12:30:00 = 12.5 hours)
        JulianDay utc = JulianDay.fromGregorian(2020, 6, 21, 12.5);
        
        // Convert to TT for calculations
        JulianDay tt = TimeConversion.utcToTT(utc);
        
        // Convert to TDB for solar system calculations
        JulianDay tdb = TimeConversion.ttToTDB(tt);
        
        // Get Delta T
        double deltaT = TimeConversion.getDeltaTSeconds(utc);
        
        // All conversions should work
        assertThat(tt.getValue()).isGreaterThan(utc.getValue());
        assertThat(Math.abs(tdb.getValue() - tt.getValue())).isLessThan(3e-5);
        assertThat(deltaT).isBetween(68.0, 71.0);
    }

    @Test
    @DisplayName("Round trip conversions from Java")
    public void testRoundTripFromJava() {
        JulianDay original = JulianDay.fromGregorian(2020, 1, 1, 0.0);
        
        // UT → TT → TDB → TT → UT
        JulianDay tt1 = TimeConversion.utToTT(original);
        JulianDay tdb = TimeConversion.ttToTDB(tt1);
        JulianDay tt2 = TimeConversion.tdbToTT(tdb);
        JulianDay final_ = TimeConversion.ttToUT(tt2);
        
        // Should recover original value
        assertThat(final_.getValue()).isCloseTo(original.getValue(), offset(1e-8));
    }
}
