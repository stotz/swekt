package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.Planet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java interop validation tests for Planet against SwissEphNet reference data
 * 
 * These tests validate that swekt's Kotlin Planet enum works correctly from Java
 * and matches SwissEphNet behavior when accessed from Java code.
 * 
 * Reference: SwissEphNet 2.08 (C# library)
 */
@DisplayName("Planet Java Interop - SwissEphNet Validation")
public class PlanetSwissEphNetJavaInteropTest {

    /**
     * Test planet constants are accessible from Java
     */
    @Test
    void planetConstantsShouldBeAccessibleFromJava() {
        assertThat(Planet.SUN).isNotNull();
        assertThat(Planet.MOON).isNotNull();
        assertThat(Planet.MERCURY).isNotNull();
        assertThat(Planet.VENUS).isNotNull();
        assertThat(Planet.MARS).isNotNull();
        assertThat(Planet.JUPITER).isNotNull();
        assertThat(Planet.SATURN).isNotNull();
        assertThat(Planet.URANUS).isNotNull();
        assertThat(Planet.NEPTUNE).isNotNull();
        assertThat(Planet.PLUTO).isNotNull();
        assertThat(Planet.MEAN_NODE).isNotNull();
        assertThat(Planet.TRUE_NODE).isNotNull();
    }

    /**
     * Test planet IDs match SwissEphNet from Java
     */
    @ParameterizedTest(name = "Planet.{0}.getId() should return {1}")
    @CsvSource({
        "SUN, 0",
        "MOON, 1",
        "MERCURY, 2",
        "VENUS, 3",
        "MARS, 4",
        "JUPITER, 5",
        "SATURN, 6",
        "URANUS, 7",
        "NEPTUNE, 8",
        "PLUTO, 9",
        "MEAN_NODE, 10",
        "TRUE_NODE, 11"
    })
    void planetIdsShouldMatchSwissEphNetFromJava(String planetName, int expectedId) {
        Planet planet = Planet.valueOf(planetName);
        
        assertThat(planet.getId())
            .describedAs("ID for Planet.%s", planetName)
            .isEqualTo(expectedId);
    }

    /**
     * Test planet display names match SwissEphNet from Java
     */
    @ParameterizedTest(name = "Planet.{0}.getDisplayName() should return '{1}'")
    @CsvSource({
        "SUN, Sun",
        "MOON, Moon",
        "MERCURY, Mercury",
        "VENUS, Venus",
        "MARS, Mars",
        "JUPITER, Jupiter",
        "SATURN, Saturn",
        "URANUS, Uranus",
        "NEPTUNE, Neptune",
        "PLUTO, Pluto",
        "MEAN_NODE, mean Node",
        "TRUE_NODE, true Node"
    })
    void planetDisplayNamesShouldMatchSwissEphNetFromJava(
        String planetName,
        String expectedDisplayName
    ) {
        Planet planet = Planet.valueOf(planetName);
        
        assertThat(planet.getDisplayName())
            .describedAs("Display name for Planet.%s", planetName)
            .isEqualTo(expectedDisplayName);
    }

    /**
     * Test fromId lookup from Java
     */
    @ParameterizedTest(name = "Planet.fromId({0}) should return {1}")
    @CsvSource({
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
    })
    void fromIdShouldWorkFromJava(int id, String expectedPlanetName) {
        Planet planet = Planet.fromId(id);
        
        assertThat(planet)
            .describedAs("Planet for ID %d", id)
            .isNotNull()
            .isEqualTo(Planet.valueOf(expectedPlanetName));
    }

    /**
     * Test fromId returns null for invalid IDs from Java
     */
    @Test
    void fromIdShouldReturnNullForInvalidIds() {
        assertThat(Planet.fromId(-1)).isNull();
        assertThat(Planet.fromId(12)).isNull();
        assertThat(Planet.fromId(100)).isNull();
        assertThat(Planet.fromId(999)).isNull();
    }

    /**
     * Test classicalPlanets from Java
     */
    @Test
    void classicalPlanetsShouldMatchSwissEphNetFromJava() {
        List<Planet> classical = Planet.classicalPlanets();
        
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
            );
    }

    /**
     * Test modernPlanets from Java
     */
    @Test
    void modernPlanetsShouldMatchSwissEphNetFromJava() {
        List<Planet> modern = Planet.modernPlanets();
        
        assertThat(modern)
            .describedAs("Modern planets")
            .hasSize(3)
            .containsExactly(
                Planet.URANUS,
                Planet.NEPTUNE,
                Planet.PLUTO
            );
    }

    /**
     * Test Planet.values() is accessible from Java
     */
    @Test
    void valuesShouldBeAccessibleFromJava() {
        Planet[] planets = Planet.values();
        
        assertThat(planets)
            .describedAs("All planets")
            .hasSize(13);  // Now includes EARTH
    }

    /**
     * Test enum name() method from Java
     */
    @Test
    void enumNameShouldWorkFromJava() {
        assertThat(Planet.SUN.name()).isEqualTo("SUN");
        assertThat(Planet.MARS.name()).isEqualTo("MARS");
        assertThat(Planet.MEAN_NODE.name()).isEqualTo("MEAN_NODE");
    }

    /**
     * Test planet ordering from Java
     */
    @Test
    void planetOrderingShouldFollowSwissEphNetFromJava() {
        assertThat(Planet.SUN.getId()).isEqualTo(0);
        assertThat(Planet.MOON.getId()).isEqualTo(1);
        assertThat(Planet.MERCURY.getId()).isEqualTo(2);
        assertThat(Planet.VENUS.getId()).isEqualTo(3);
        assertThat(Planet.MARS.getId()).isEqualTo(4);
        assertThat(Planet.JUPITER.getId()).isEqualTo(5);
        assertThat(Planet.SATURN.getId()).isEqualTo(6);
        assertThat(Planet.URANUS.getId()).isEqualTo(7);
        assertThat(Planet.NEPTUNE.getId()).isEqualTo(8);
        assertThat(Planet.PLUTO.getId()).isEqualTo(9);
        assertThat(Planet.MEAN_NODE.getId()).isEqualTo(10);
        assertThat(Planet.TRUE_NODE.getId()).isEqualTo(11);
    }

    /**
     * Test using planets in switch statement (Java)
     */
    @Test
    void planetsShouldWorkInSwitchStatement() {
        Planet planet = Planet.MARS;
        
        String result = switch (planet) {
            case SUN -> "Star";
            case MOON -> "Satellite";
            case MERCURY, VENUS, EARTH, MARS, JUPITER, SATURN, URANUS, NEPTUNE -> "Planet";
            case PLUTO -> "Dwarf Planet";
            case MEAN_NODE, TRUE_NODE -> "Lunar Node";
        };
        
        assertThat(result).isEqualTo("Planet");
    }

    /**
     * Test planet comparison from Java
     */
    @Test
    void planetComparisonShouldWorkFromJava() {
        Planet mars = Planet.MARS;
        Planet alsoMars = Planet.valueOf("MARS");
        Planet jupiter = Planet.JUPITER;
        
        assertThat(mars).isEqualTo(alsoMars);
        assertThat(mars).isNotEqualTo(jupiter);
        assertThat(mars == alsoMars).isTrue();
        assertThat(mars == jupiter).isFalse();
    }

    /**
     * Test iterating over planets from Java
     */
    @Test
    void iteratingOverPlanetsShouldWorkFromJava() {
        int count = 0;
        for (Planet planet : Planet.values()) {
            assertThat(planet.getId()).isIn(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13);  // EARTH has ID 13
            assertThat(planet.getDisplayName()).isNotEmpty();
            count++;
        }
        
        assertThat(count).isEqualTo(13);  // Now includes EARTH
    }

    /**
     * Test classical planets contain correct IDs from Java
     */
    @Test
    void classicalPlanetsIdsShouldBeCorrect() {
        List<Planet> classical = Planet.classicalPlanets();
        
        for (Planet planet : classical) {
            assertThat(planet.getId())
                .describedAs("Classical planet ID for %s", planet.name())
                .isBetween(0, 6);
        }
    }

    /**
     * Test modern planets contain correct IDs from Java
     */
    @Test
    void modernPlanetsIdsShouldBeCorrect() {
        List<Planet> modern = Planet.modernPlanets();
        
        for (Planet planet : modern) {
            assertThat(planet.getId())
                .describedAs("Modern planet ID for %s", planet.name())
                .isBetween(7, 9);
        }
    }

    /**
     * Test all planet IDs are unique from Java
     */
    @Test
    void allPlanetIdsShouldBeUnique() {
        Planet[] planets = Planet.values();
        long uniqueIds = Arrays.stream(planets)
            .map(Planet::getId)
            .distinct()
            .count();
        
        assertThat(uniqueIds)
            .describedAs("Number of unique planet IDs")
            .isEqualTo(planets.length);
    }

    /**
     * Test fromId for all enum values from Java
     */
    @Test
    void fromIdShouldWorkForAllEnumValues() {
        for (Planet planet : Planet.values()) {
            Planet found = Planet.fromId(planet.getId());
            
            assertThat(found)
                .describedAs("Lookup of %s by ID %d", planet.name(), planet.getId())
                .isNotNull()
                .isEqualTo(planet);
        }
    }

    /**
     * Test planet properties are immutable from Java
     */
    @Test
    void planetPropertiesShouldBeImmutable() {
        Planet mars = Planet.MARS;
        
        // Get properties multiple times
        int id1 = mars.getId();
        int id2 = mars.getId();
        String name1 = mars.getDisplayName();
        String name2 = mars.getDisplayName();
        
        // Should be identical
        assertThat(id1).isEqualTo(id2);
        assertThat(name1).isEqualTo(name2);
    }

    /**
     * Test planet toString from Java
     */
    @Test
    void toStringShouldWorkFromJava() {
        assertThat(Planet.MARS.toString()).contains("MARS");
        assertThat(Planet.JUPITER.toString()).contains("JUPITER");
    }

    /**
     * Test planet ordinal from Java
     */
    @Test
    void ordinalShouldWorkFromJava() {
        // Ordinal is enum position, not Swiss Ephemeris ID
        assertThat(Planet.SUN.ordinal()).isEqualTo(0);
        assertThat(Planet.MOON.ordinal()).isEqualTo(1);
        
        // Ordinal != ID in all cases
        // But for our implementation they should match for planets 0-11
        Planet[] planets = Planet.values();
        for (int i = 0; i < planets.length; i++) {
            Planet planet = planets[i];
            assertThat(planet.ordinal()).isEqualTo(i);
        }
    }
}
