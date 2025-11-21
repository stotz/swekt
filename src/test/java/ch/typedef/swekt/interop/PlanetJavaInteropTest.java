package ch.typedef.swekt.interop;

import ch.typedef.swekt.model.Planet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Tests for Java interoperability with Planet enum.
 * 
 * Verifies that the Planet enum works naturally from Java.
 */
public class PlanetJavaInteropTest {

    @Test
    public void shouldAccessPlanetEnumValues() {
        Planet mars = Planet.MARS;
        
        assertThat(mars.getId()).isEqualTo(4);
        assertThat(mars.getDisplayName()).isEqualTo("Mars");
    }

    @Test
    public void shouldAccessAllEnumValues() {
        Planet[] planets = Planet.values();
        
        assertThat(planets).hasSize(12);
        assertThat(planets[0]).isEqualTo(Planet.SUN);
    }

    @Test
    public void shouldLookupPlanetById() {
        Planet planet = Planet.fromId(4);
        
        assertThat(planet).isEqualTo(Planet.MARS);
    }

    @Test
    public void shouldReturnNullForInvalidId() {
        Planet planet = Planet.fromId(999);
        
        assertThat(planet).isNull();
    }

    @Test
    public void shouldGetMainPlanets() {
        List<Planet> mainPlanets = Planet.mainPlanets();
        
        assertThat(mainPlanets).hasSize(10);
        assertThat(mainPlanets).contains(Planet.SUN, Planet.MARS, Planet.PLUTO);
        assertThat(mainPlanets).doesNotContain(Planet.MEAN_NODE);
    }

    @Test
    public void shouldGetClassicalPlanets() {
        List<Planet> classical = Planet.classicalPlanets();
        
        assertThat(classical).hasSize(7);
        assertThat(classical).contains(Planet.SUN, Planet.SATURN);
        assertThat(classical).doesNotContain(Planet.URANUS, Planet.NEPTUNE);
    }

    @Test
    public void shouldCheckPlanetProperties() {
        assertThat(Planet.MARS.isClassical()).isTrue();
        assertThat(Planet.MARS.isModern()).isFalse();
        assertThat(Planet.MARS.isNode()).isFalse();
        
        assertThat(Planet.URANUS.isClassical()).isFalse();
        assertThat(Planet.URANUS.isModern()).isTrue();
        
        assertThat(Planet.MEAN_NODE.isNode()).isTrue();
    }

    @Test
    public void shouldUseInSwitchStatement() {
        Planet planet = Planet.MARS;
        String result;
        
        switch (planet) {
            case SUN:
                result = "Star";
                break;
            case MARS:
                result = "Red Planet";
                break;
            default:
                result = "Other";
                break;
        }
        
        assertThat(result).isEqualTo("Red Planet");
    }
}
