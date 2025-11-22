package ch.typedef.swekt.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

/**
 * Cross-validation tests against SwissEphNet reference data
 * 
 * These tests validate swekt's Planet enum implementation against known correct values
 * from the SwissEphNet library (C# port of Swiss Ephemeris).
 * 
 * Reference: SwissEphNet 2.08, SwissEphTest.Planets.cs
 */
@DisplayName("Planet Cross-Validation with SwissEphNet")
class PlanetSwissEphNetValidationTest {

    /**
     * Core planet IDs and names from SwissEphNet
     * Reference: SwissEphTest.Planets.cs:13-22
     */
    @ParameterizedTest(name = "Planet ID {0} should be {1}")
    @CsvSource(
        "0, SUN, Sun",
        "1, MOON, Moon",
        "2, MERCURY, Mercury",
        "3, VENUS, Venus",
        "4, MARS, Mars",
        "5, JUPITER, Jupiter",
        "6, SATURN, Saturn",
        "7, URANUS, Uranus",
        "8, NEPTUNE, Neptune",
        "9, PLUTO, Pluto"
    )
    fun `planet IDs should match SwissEphNet core planets`(
        id: Int,
        planetName: String,
        displayName: String
    ) {
        val planet = Planet.valueOf(planetName)
        
        assertThat(planet.id)
            .describedAs("ID for Planet.$planetName")
            .isEqualTo(id)
        
        assertThat(planet.displayName)
            .describedAs("Display name for Planet.$planetName")
            .isEqualTo(displayName)
    }

    /**
     * Lunar nodes from SwissEphNet
     * Reference: SwissEphTest.Planets.cs:25-26
     */
    @ParameterizedTest(name = "Planet ID {0} should be {1}")
    @CsvSource(
        "10, MEAN_NODE, mean Node",
        "11, TRUE_NODE, true Node"
    )
    fun `lunar node IDs should match SwissEphNet`(
        id: Int,
        planetName: String,
        displayName: String
    ) {
        val planet = Planet.valueOf(planetName)
        
        assertThat(planet.id)
            .describedAs("ID for Planet.$planetName")
            .isEqualTo(id)
        
        assertThat(planet.displayName)
            .describedAs("Display name for Planet.$planetName")
            .isEqualTo(displayName)
    }

    /**
     * Test fromId lookup against SwissEphNet
     * Reference: SwissEphTest.Planets.cs
     */
    @ParameterizedTest(name = "fromId({0}) should return {1}")
    @CsvSource(
        "0, SUN",
        "1, MOON",
        "2, MERCURY",
        "3, VENUS",
        "4, MARS",
        "5, JUPITER",
        "6, SATURN",
        "7, URANUS",
        "8, NEPTUNE",
        "9, PLUTO",
        "10, MEAN_NODE",
        "11, TRUE_NODE"
    )
    fun `fromId should match SwissEphNet planet lookup`(
        id: Int,
        expectedPlanet: String
    ) {
        val planet = Planet.fromId(id)
        
        assertThat(planet)
            .describedAs("Planet for ID $id")
            .isNotNull()
            .isEqualTo(Planet.valueOf(expectedPlanet))
    }

    /**
     * Test invalid IDs return null
     * SwissEphNet returns "name not found" for invalid IDs
     */
    @Test
    fun `fromId should return null for invalid IDs`() {
        assertThat(Planet.fromId(-1)).isNull()
        assertThat(Planet.fromId(12)).isNull()
        assertThat(Planet.fromId(100)).isNull()
        assertThat(Planet.fromId(999)).isNull()
    }

    /**
     * Test classical planets method
     * SwissEphNet SE_SUN through SE_SATURN (0-6)
     */
    @Test
    fun `classicalPlanets should match SwissEphNet classical set`() {
        val classical = Planet.classicalPlanets()
        
        assertThat(classical)
            .describedAs("Classical planets")
            .hasSize(7)
            .containsExactly(
                Planet.SUN,
                Planet.MOON,
                Planet.MERCURY,
                Planet.VENUS,
                Planet.MARS,
                Planet.JUPITER,
                Planet.SATURN
            )
        
        // Verify IDs are 0-6
        classical.forEach { planet ->
            assertThat(planet.id)
                .describedAs("Classical planet ID for ${planet.name}")
                .isBetween(0, 6)
        }
    }

    /**
     * Test modern planets method
     * SwissEphNet SE_URANUS, SE_NEPTUNE, SE_PLUTO (7-9)
     */
    @Test
    fun `modernPlanets should match SwissEphNet modern set`() {
        val modern = Planet.modernPlanets()
        
        assertThat(modern)
            .describedAs("Modern planets")
            .hasSize(3)
            .containsExactly(
                Planet.URANUS,
                Planet.NEPTUNE,
                Planet.PLUTO
            )
        
        // Verify IDs are 7-9
        modern.forEach { planet: Planet ->
            assertThat(planet.id)
                .describedAs("Modern planet ID for ${planet.name}")
                .isBetween(7, 9)
        }
    }

    /**
     * Test that all planet IDs are unique
     * SwissEphNet uses unique constants for each planet
     */
    @Test
    fun `all planet IDs should be unique`() {
        val allIds = Planet.entries.map { it.id }
        val uniqueIds = allIds.toSet()
        
        assertThat(uniqueIds)
            .describedAs("Unique planet IDs")
            .hasSize(allIds.size)
    }

    /**
     * Test planet ordering matches SwissEphNet constants
     * Swiss Ephemeris has a specific ordering: SUN=0, MOON=1, etc.
     */
    @Test
    fun `planet IDs should follow SwissEphNet ordering`() {
        assertThat(Planet.SUN.id).isEqualTo(0)
        assertThat(Planet.MOON.id).isEqualTo(1)
        assertThat(Planet.MERCURY.id).isEqualTo(2)
        assertThat(Planet.VENUS.id).isEqualTo(3)
        assertThat(Planet.MARS.id).isEqualTo(4)
        assertThat(Planet.JUPITER.id).isEqualTo(5)
        assertThat(Planet.SATURN.id).isEqualTo(6)
        assertThat(Planet.URANUS.id).isEqualTo(7)
        assertThat(Planet.NEPTUNE.id).isEqualTo(8)
        assertThat(Planet.PLUTO.id).isEqualTo(9)
        assertThat(Planet.MEAN_NODE.id).isEqualTo(10)
        assertThat(Planet.TRUE_NODE.id).isEqualTo(11)
    }

    /**
     * Test planet display names match SwissEphNet exactly
     * Reference: SwissEphTest.Planets.cs:13-26
     */
    @Test
    fun `planet display names should match SwissEphNet exactly`() {
        assertThat(Planet.SUN.displayName).isEqualTo("Sun")
        assertThat(Planet.MOON.displayName).isEqualTo("Moon")
        assertThat(Planet.MERCURY.displayName).isEqualTo("Mercury")
        assertThat(Planet.VENUS.displayName).isEqualTo("Venus")
        assertThat(Planet.MARS.displayName).isEqualTo("Mars")
        assertThat(Planet.JUPITER.displayName).isEqualTo("Jupiter")
        assertThat(Planet.SATURN.displayName).isEqualTo("Saturn")
        assertThat(Planet.URANUS.displayName).isEqualTo("Uranus")
        assertThat(Planet.NEPTUNE.displayName).isEqualTo("Neptune")
        assertThat(Planet.PLUTO.displayName).isEqualTo("Pluto")
        assertThat(Planet.MEAN_NODE.displayName).isEqualTo("mean Node")
        assertThat(Planet.TRUE_NODE.displayName).isEqualTo("true Node")
    }

    /**
     * Test Earth constant
     * SwissEphNet SE_EARTH = 14, but we don't include it as a planet in calculations
     * (it's the observer position, not a calculable body)
     */
    @Test
    fun `Earth ID 14 should not be in planet enum`() {
        // Earth (ID 14) should not be in our Planet enum
        assertThat(Planet.fromId(14)).isNull()
        
        // All planet IDs should be < 14 or in node range (10-11)
        Planet.entries.forEach { planet ->
            if (planet.id >= 10) {
                assertThat(planet.id)
                    .describedAs("Planet ${planet.name} ID")
                    .isIn(10, 11) // Only nodes have IDs >= 10
            } else {
                assertThat(planet.id)
                    .describedAs("Planet ${planet.name} ID")
                    .isLessThan(10)
            }
        }
    }

    /**
     * Validate total number of planets matches our design
     * SwissEphNet has many more bodies, but we focus on core planets + nodes
     */
    @Test
    fun `should have exactly 12 planets in enum`() {
        assertThat(Planet.entries)
            .describedAs("Total number of planets")
            .hasSize(12)
    }

    /**
     * Test that classical + modern planets cover main bodies
     */
    @Test
    fun `classical plus modern should cover main planets`() {
        val classical = Planet.classicalPlanets()
        val modern = Planet.modernPlanets()
        val mainPlanets = classical + modern
        
        assertThat(mainPlanets)
            .describedAs("Main planets (classical + modern)")
            .hasSize(10)
            .contains(
                Planet.SUN,
                Planet.MOON,
                Planet.MERCURY,
                Planet.VENUS,
                Planet.MARS,
                Planet.JUPITER,
                Planet.SATURN,
                Planet.URANUS,
                Planet.NEPTUNE,
                Planet.PLUTO
            )
    }

    /**
     * Test planet name consistency
     * Enum name should match display name (except for special cases)
     */
    @Test
    fun `planet enum names should be consistent with display names`() {
        Planet.entries.forEach { planet: Planet ->
            when (planet) {
                Planet.MEAN_NODE, Planet.TRUE_NODE -> {
                    // Nodes have different naming convention
                    assertThat(planet.displayName).contains("Node")
                }
                else -> {
                    // Regular planets: enum name should match display name (uppercase)
                    assertThat(planet.name)
                        .describedAs("Enum name for ${planet.displayName}")
                        .isEqualTo(planet.displayName.uppercase())
                }
            }
        }
    }

    /**
     * Test that all planets can be looked up by ID
     * This is critical for file I/O where files use numeric IDs
     */
    @Test
    fun `all planets should be findable by their ID`() {
        Planet.entries.forEach { planet: Planet ->
            val found = Planet.fromId(planet.id)
            
            assertThat(found)
                .describedAs("Lookup of ${planet.name} by ID ${planet.id}")
                .isNotNull()
                .isEqualTo(planet)
        }
    }

    /**
     * Test ID range coverage
     * SwissEphNet uses IDs 0-11 for our supported planets
     */
    @Test
    fun `planet IDs should cover SwissEphNet range 0-11`() {
        val allIds = Planet.entries.map { it.id }.sorted()
        
        assertThat(allIds)
            .describedAs("All planet IDs")
            .containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    }

    /**
     * Test planet groups are mutually exclusive
     */
    @Test
    fun `classical and modern planets should not overlap`() {
        val classical = Planet.classicalPlanets().toSet()
        val modern = Planet.modernPlanets().toSet()
        
        assertThat(classical.intersect(modern))
            .describedAs("Overlap between classical and modern")
            .isEmpty()
    }

    /**
     * Test nodes are separate from main planets
     */
    @Test
    fun `nodes should be separate from main planets`() {
        val nodes = listOf(Planet.MEAN_NODE, Planet.TRUE_NODE)
        val mainPlanets = (Planet.classicalPlanets() + Planet.modernPlanets()).toSet()
        
        nodes.forEach { node: Planet ->
            assertThat(mainPlanets)
                .describedAs("Main planets")
                .doesNotContain(node)
        }
    }
}
