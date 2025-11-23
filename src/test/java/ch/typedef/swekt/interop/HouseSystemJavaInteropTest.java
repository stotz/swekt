package ch.typedef.swekt.interop;

import ch.typedef.swekt.houses.*;
import ch.typedef.swekt.model.JulianDay;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

@DisplayName("House System Java Interop Tests")
public class HouseSystemJavaInteropTest {

    private final HouseCalculator calculator = new StandardHouseCalculator();

    @Test
    @DisplayName("Calculate Equal Houses from Java")
    public void testEqualHousesFromJava() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        HouseCusps houses = calculator.calculate(jd, location, HouseSystem.EQUAL);

        // Check that we got valid cusps
        assertThat(houses.getAscendant()).isBetween(0.0, 360.0);
        assertThat(houses.getMc()).isBetween(0.0, 360.0);

        // Check that houses are 30° apart
        for (int i = 2; i <= 12; i++) {
            double diff = (houses.getCusp(i) - houses.getCusp(i - 1) + 360.0) % 360.0;
            assertThat(diff).isCloseTo(30.0, offset(0.01));
        }
    }

    @Test
    @DisplayName("Calculate Porphyry Houses from Java")
    public void testPorphyryHousesFromJava() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        HouseCusps houses = calculator.calculate(jd, location, HouseSystem.PORPHYRY);

        assertThat(houses.getAscendant()).isBetween(0.0, 360.0);
        assertThat(houses.getMc()).isBetween(0.0, 360.0);
        assertThat(houses.getCusp(1)).isCloseTo(houses.getAscendant(), offset(0.01));
        assertThat(houses.getCusp(10)).isCloseTo(houses.getMc(), offset(0.01));
    }

    @Test
    @DisplayName("Calculate Whole Sign Houses from Java")
    public void testWholeSignHousesFromJava() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        HouseCusps houses = calculator.calculate(jd, location, HouseSystem.WHOLE_SIGN);

        // Each cusp should be at a sign boundary (0°, 30°, 60°, etc.)
        for (int i = 1; i <= 12; i++) {
            double cusp = houses.getCusp(i);
            double remainder = cusp % 30.0;
            assertThat(remainder).isCloseTo(0.0, offset(0.01));
        }
    }

    @Test
    @DisplayName("House system lookup by code from Java")
    public void testHouseSystemFromCode() {
        assertThat(HouseSystem.fromCode('P')).isEqualTo(HouseSystem.PLACIDUS);
        assertThat(HouseSystem.fromCode('K')).isEqualTo(HouseSystem.KOCH);
        assertThat(HouseSystem.fromCode('O')).isEqualTo(HouseSystem.PORPHYRY);
        assertThat(HouseSystem.fromCode('A')).isEqualTo(HouseSystem.EQUAL);
        assertThat(HouseSystem.fromCode('W')).isEqualTo(HouseSystem.WHOLE_SIGN);

        // Case insensitive
        assertThat(HouseSystem.fromCode('p')).isEqualTo(HouseSystem.PLACIDUS);
    }

    @Test
    @DisplayName("House system lookup by name from Java")
    public void testHouseSystemFromName() {
        assertThat(HouseSystem.fromName("Placidus")).isEqualTo(HouseSystem.PLACIDUS);
        assertThat(HouseSystem.fromName("Koch")).isEqualTo(HouseSystem.KOCH);
        assertThat(HouseSystem.fromName("Equal")).isEqualTo(HouseSystem.EQUAL);

        // Case insensitive
        assertThat(HouseSystem.fromName("placidus")).isEqualTo(HouseSystem.PLACIDUS);
    }

    @Test
    @DisplayName("Geographic location from Java")
    public void testGeographicLocation() {
        GeographicLocation location = new GeographicLocation(51.5, -0.1, 0.0);

        assertThat(location.getLatitude()).isCloseTo(51.5, offset(0.01));
        assertThat(location.getLongitude()).isCloseTo(-0.1, offset(0.01));
        assertThat(location.getElevation()).isCloseTo(0.0, offset(0.01));
    }

    @Test
    @DisplayName("Location from DMS from Java")
    public void testLocationFromDMS() {
        // Greenwich: 51°28'38"N, 0°0'0"E
        GeographicLocation greenwich = GeographicLocation.fromDMS(
                51, 28, 38.0, true,  // latitude
                0, 0, 0.0, true,      // longitude
                0.0                    // elevation
        );

        assertThat(greenwich.getLatitude()).isCloseTo(51.4772, offset(0.01));
        assertThat(greenwich.getLongitude()).isCloseTo(0.0, offset(0.01));
    }

    @Test
    @DisplayName("Descendant and IC from Java")
    public void testDescendantAndIC() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        HouseCusps houses = calculator.calculate(jd, location, HouseSystem.EQUAL);

        double expectedDesc = (houses.getAscendant() + 180.0) % 360.0;
        double expectedIC = (houses.getMc() + 180.0) % 360.0;

        assertThat(houses.getDescendant()).isCloseTo(expectedDesc, offset(0.01));
        assertThat(houses.getIc()).isCloseTo(expectedIC, offset(0.01));
    }

    @Test
    @DisplayName("Different locations give different cusps")
    public void testDifferentLocations() {
        JulianDay jd = JulianDay.getJ2000();

        HouseCusps greenwich = calculator.calculate(
                jd, GeographicLocation.GREENWICH, HouseSystem.EQUAL
        );
        HouseCusps newYork = calculator.calculate(
                jd, GeographicLocation.NEW_YORK, HouseSystem.EQUAL
        );
        HouseCusps tokyo = calculator.calculate(
                jd, GeographicLocation.TOKYO, HouseSystem.EQUAL
        );

        // Different locations should have different Ascendants
        assertThat(greenwich.getAscendant()).isNotCloseTo(newYork.getAscendant(), offset(1.0));
        assertThat(newYork.getAscendant()).isNotCloseTo(tokyo.getAscendant(), offset(1.0));
    }

    @Test
    @DisplayName("Gauquelin Sectors from Java")
    public void testGauquelinSectors() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        HouseCusps houses = calculator.calculate(jd, location, HouseSystem.GAUQUELIN);

        // Should have 36 sectors (array size 37 including index 0)
        assertThat(houses.getCusps().length).isEqualTo(37);

        // Each sector should be exactly 10°
        for (int i = 2; i <= 36; i++) {
            double diff = (houses.getCusp(i) - houses.getCusp(i - 1) + 360.0) % 360.0;
            assertThat(diff).isCloseTo(10.0, offset(0.01));
        }
    }

    @Test
    @DisplayName("All house systems are accessible from Java")
    public void testAllHouseSystemsAccessible() {
        JulianDay jd = JulianDay.getJ2000();
        GeographicLocation location = GeographicLocation.GREENWICH;

        // Test that all systems can be calculated without errors
        for (HouseSystem system : HouseSystem.values()) {
            HouseCusps houses = calculator.calculate(jd, location, system);

            assertThat(houses.getAscendant()).isBetween(0.0, 360.0);
            assertThat(houses.getMc()).isBetween(0.0, 360.0);

            // Verify we have the right number of cusps
            int expectedSize = (system == HouseSystem.GAUQUELIN) ? 37 : 13;
            assertThat(houses.getCusps().length).isEqualTo(expectedSize);
        }
    }
}
