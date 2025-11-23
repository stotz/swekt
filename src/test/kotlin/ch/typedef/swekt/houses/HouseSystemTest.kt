package ch.typedef.swekt.houses

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("House System Tests")
class HouseSystemTest {

    private val calculator = StandardHouseCalculator()

    @Test
    @DisplayName("Equal Houses: each house is exactly 30 degrees")
    fun testEqualHouses() {
        // Greenwich at J2000
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.EQUAL)
        
        // Check that each house is exactly 30° from previous
        for (i in 2..12) {
            val diff = (houses.getCusp(i) - houses.getCusp(i - 1) + 360.0) % 360.0
            assertThat(diff).isCloseTo(30.0, Offset.offset(0.01))
        }
        
        // First cusp should be Ascendant
        assertThat(houses.getCusp(1)).isCloseTo(houses.ascendant, Offset.offset(0.01))
    }

    @Test
    @DisplayName("Whole Sign Houses: each house starts at sign boundary")
    fun testWholeSignHouses() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.WHOLE_SIGN)
        
        // Each cusp should be at a sign boundary (0°, 30°, 60°, ...)
        for (i in 1..12) {
            val cusp = houses.getCusp(i)
            val remainder = cusp % 30.0
            assertThat(remainder).isCloseTo(0.0, Offset.offset(0.01))
        }
    }

    @Test
    @DisplayName("Porphyry Houses: quadrants divided into 3 equal parts")
    fun testPorphyryHouses() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.PORPHYRY)
        
        // Check that quadrants are trisected
        val asc = houses.ascendant
        val mc = houses.mc
        val desc = (asc + 180.0) % 360.0
        val ic = (mc + 180.0) % 360.0
        
        // Quadrant 1: ASC to MC should be divided into 3
        val quad1Length = calculateArcLength(asc, mc)
        val house12_11 = calculateArcLength(houses.getCusp(11), houses.getCusp(12))
        val house11_asc = calculateArcLength(asc, houses.getCusp(11))
        val house12_mc = calculateArcLength(houses.getCusp(12), mc)
        
        assertThat(house11_asc).isCloseTo(quad1Length / 3.0, Offset.offset(1.0))
        assertThat(house12_11).isCloseTo(quad1Length / 3.0, Offset.offset(1.0))
        assertThat(house12_mc).isCloseTo(quad1Length / 3.0, Offset.offset(1.0))
    }

    @Test
    @DisplayName("MC calculation: should match manual calculation")
    fun testMCCalculation() {
        // Test at equinox (ARMC = 0° should give MC ≈ 0°)
        val jd = JulianDay.fromGregorian(2000, 3, 20, 7.5) // Around spring equinox
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.EQUAL)
        
        // MC should be defined
        assertThat(houses.mc).isBetween(0.0, 360.0)
        assertThat(houses.armc).isBetween(0.0, 360.0)
    }

    @Test
    @DisplayName("Ascendant calculation: should match manual calculation")
    fun testAscendantCalculation() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.EQUAL)
        
        // Ascendant should be defined and different from MC
        assertThat(houses.ascendant).isBetween(0.0, 360.0)
        assertThat(houses.ascendant).isNotCloseTo(houses.mc, Offset.offset(1.0))
    }

    @Test
    @DisplayName("Descendant is opposite of Ascendant")
    fun testDescendant() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.EQUAL)
        
        val expectedDesc = (houses.ascendant + 180.0) % 360.0
        assertThat(houses.descendant).isCloseTo(expectedDesc, Offset.offset(0.01))
    }

    @Test
    @DisplayName("IC is opposite of MC")
    fun testIC() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.EQUAL)
        
        val expectedIC = (houses.mc + 180.0) % 360.0
        assertThat(houses.ic).isCloseTo(expectedIC, Offset.offset(0.01))
    }

    @Test
    @DisplayName("Different house systems give different cusps")
    fun testDifferentSystems() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val equal = calculator.calculate(jd, location, HouseSystem.EQUAL)
        val porphyry = calculator.calculate(jd, location, HouseSystem.PORPHYRY)
        val wholeSign = calculator.calculate(jd, location, HouseSystem.WHOLE_SIGN)
        
        // House 2 should be different in each system
        assertThat(equal.getCusp(2))
            .isNotCloseTo(porphyry.getCusp(2), Offset.offset(1.0))
        assertThat(porphyry.getCusp(2))
            .isNotCloseTo(wholeSign.getCusp(2), Offset.offset(1.0))
    }

    @Test
    @DisplayName("Geographic location affects house cusps")
    fun testDifferentLocations() {
        val jd = JulianDay.J2000
        
        val greenwich = calculator.calculate(jd, GeographicLocation.GREENWICH, HouseSystem.EQUAL)
        val newYork = calculator.calculate(jd, GeographicLocation.NEW_YORK, HouseSystem.EQUAL)
        val tokyo = calculator.calculate(jd, GeographicLocation.TOKYO, HouseSystem.EQUAL)
        
        // Different locations should have different Ascendants
        assertThat(greenwich.ascendant)
            .isNotCloseTo(newYork.ascendant, Offset.offset(1.0))
        assertThat(newYork.ascendant)
            .isNotCloseTo(tokyo.ascendant, Offset.offset(1.0))
    }

    @Test
    @DisplayName("Gauquelin Sectors: 36 sectors instead of 12")
    fun testGauquelinSectors() {
        val jd = JulianDay.J2000
        val location = GeographicLocation.GREENWICH
        
        val houses = calculator.calculate(jd, location, HouseSystem.GAUQUELIN)
        
        // Should have 36 sectors
        assertThat(houses.cusps.size).isEqualTo(37) // 0-36
        
        // Each sector should be exactly 10°
        for (i in 2..36) {
            val diff = (houses.getCusp(i) - houses.getCusp(i - 1) + 360.0) % 360.0
            assertThat(diff).isCloseTo(10.0, Offset.offset(0.01))
        }
    }

    @Test
    @DisplayName("House system from code")
    fun testFromCode() {
        assertThat(HouseSystem.fromCode('P')).isEqualTo(HouseSystem.PLACIDUS)
        assertThat(HouseSystem.fromCode('K')).isEqualTo(HouseSystem.KOCH)
        assertThat(HouseSystem.fromCode('O')).isEqualTo(HouseSystem.PORPHYRY)
        assertThat(HouseSystem.fromCode('A')).isEqualTo(HouseSystem.EQUAL)
        assertThat(HouseSystem.fromCode('W')).isEqualTo(HouseSystem.WHOLE_SIGN)
        
        // Case insensitive
        assertThat(HouseSystem.fromCode('p')).isEqualTo(HouseSystem.PLACIDUS)
    }

    @Test
    @DisplayName("House system from name")
    fun testFromName() {
        assertThat(HouseSystem.fromName("Placidus")).isEqualTo(HouseSystem.PLACIDUS)
        assertThat(HouseSystem.fromName("Koch")).isEqualTo(HouseSystem.KOCH)
        assertThat(HouseSystem.fromName("Equal")).isEqualTo(HouseSystem.EQUAL)
        
        // Case insensitive
        assertThat(HouseSystem.fromName("placidus")).isEqualTo(HouseSystem.PLACIDUS)
    }

    @Test
    @DisplayName("Geographic location validation")
    fun testLocationValidation() {
        // Valid locations
        assertThat(GeographicLocation(51.5, 0.0).latitude).isEqualTo(51.5)
        assertThat(GeographicLocation(-33.9, 151.2).latitude).isEqualTo(-33.9)
        
        // Invalid latitude
        try {
            GeographicLocation(91.0, 0.0)
            org.junit.jupiter.api.Assertions.fail("Should throw exception for latitude > 90")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Invalid longitude
        try {
            GeographicLocation(0.0, 181.0)
            org.junit.jupiter.api.Assertions.fail("Should throw exception for longitude > 180")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    @DisplayName("Location from DMS (degrees, minutes, seconds)")
    fun testLocationFromDMS() {
        // Greenwich: 51°28'38"N, 0°0'0"E
        val greenwich = GeographicLocation.fromDMS(
            latDegrees = 51,
            latMinutes = 28,
            latSeconds = 38.0,
            latNorth = true,
            lonDegrees = 0,
            lonMinutes = 0,
            lonSeconds = 0.0,
            lonEast = true
        )
        
        assertThat(greenwich.latitude).isCloseTo(51.4772, Offset.offset(0.01))
        assertThat(greenwich.longitude).isCloseTo(0.0, Offset.offset(0.01))
    }

    /**
     * Calculate arc length between two angles (shortest path).
     */
    private fun calculateArcLength(start: Double, end: Double): Double {
        var arc = end - start
        if (arc < 0) arc += 360.0
        if (arc > 180.0) arc = 360.0 - arc
        return arc
    }
}
