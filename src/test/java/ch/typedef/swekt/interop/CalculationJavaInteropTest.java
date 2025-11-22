package ch.typedef.swekt.interop;

import ch.typedef.swekt.calculation.PlanetaryPosition;
import ch.typedef.swekt.calculation.SimpleCalculationEngine;
import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.model.Planet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Java interoperability tests for calculation package.
 * Ensures Kotlin calculation code is fully usable from Java.
 */
public class CalculationJavaInteropTest {

    @Test
    public void testPlanetaryPositionFromJava() {
        JulianDay jd = new JulianDay(2451545.0);

        PlanetaryPosition position = new PlanetaryPosition(
                Planet.MARS,
                jd,
                355.45,
                1.85,
                1.38,
                0.5,
                0.01,
                -0.001
        );

        assertEquals(Planet.MARS, position.getPlanet());
        assertEquals(jd, position.getJulianDay());
        assertEquals(355.45, position.getLongitude(), 0.001);
        assertEquals(1.85, position.getLatitude(), 0.001);
        assertEquals(1.38, position.getDistance(), 0.001);
    }

    @Test
    public void testSimpleCalculationEngineFromJava() {
        SimpleCalculationEngine engine = new SimpleCalculationEngine();
        JulianDay jd = JulianDay.getJ2000();

        // Calculate Sun
        PlanetaryPosition sunPos = engine.calculate(Planet.SUN, jd);
        assertNotNull(sunPos);
        assertEquals(Planet.SUN, sunPos.getPlanet());
        assertTrue(sunPos.getLongitude() >= 0.0 && sunPos.getLongitude() <= 360.0);
        assertEquals(0.0, sunPos.getLatitude(), 0.001);
        assertTrue(sunPos.getDistance() > 0.0);

        // Calculate Moon
        PlanetaryPosition moonPos = engine.calculate(Planet.MOON, jd);
        assertNotNull(moonPos);
        assertEquals(Planet.MOON, moonPos.getPlanet());
        assertTrue(moonPos.getLongitude() >= 0.0 && moonPos.getLongitude() <= 360.0);
        assertTrue(moonPos.getDistance() > 0.0);
    }

    @Test
    public void testCalculationThroughoutYear() {
        SimpleCalculationEngine engine = new SimpleCalculationEngine();
        int year = 2024;

        for (int month = 1; month <= 12; month++) {
            JulianDay jd = JulianDay.fromGregorian(year, month, 15, 12.0);
            PlanetaryPosition pos = engine.calculate(Planet.SUN, jd);

            assertNotNull(pos);
            assertTrue(pos.getLongitude() >= 0.0 && pos.getLongitude() <= 360.0);
            assertEquals(0.0, pos.getLatitude(), 0.001);
        }
    }

    @Test
    public void testPlanetaryPositionEquality() {
        JulianDay jd = new JulianDay(2451545.0);

        PlanetaryPosition pos1 = new PlanetaryPosition(
                Planet.MARS, jd, 100.0, 1.0, 1.5, 0.5, 0.01, 0.0
        );

        PlanetaryPosition pos2 = new PlanetaryPosition(
                Planet.MARS, jd, 100.0, 1.0, 1.5, 0.5, 0.01, 0.0
        );

        assertEquals(pos1, pos2);
        assertEquals(pos1.hashCode(), pos2.hashCode());
    }

    @Test
    public void testPlanetaryPositionToString() {
        JulianDay jd = new JulianDay(2451545.0);
        PlanetaryPosition position = new PlanetaryPosition(
                Planet.MARS, jd, 355.45, 1.85, 1.38, 0.5, 0.01, -0.001
        );

        String str = position.toString();
        assertTrue(str.contains("MARS"), "toString should contain planet name: " + str);
        assertTrue(str.contains("355.45"), "toString should contain longitude: " + str);
    }

    @Test
    public void testUnsupportedPlanetThrowsException() {
        SimpleCalculationEngine engine = new SimpleCalculationEngine();
        JulianDay jd = JulianDay.getJ2000();

        assertThrows(UnsupportedOperationException.class, () -> {
            engine.calculate(Planet.MERCURY, jd);
        });

        assertThrows(UnsupportedOperationException.class, () -> {
            engine.calculate(Planet.JUPITER, jd);
        });
    }
}
