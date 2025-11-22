package ch.typedef.swekt.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD Tests for Planet enum
 */
class PlanetTest {

    @Test
    fun `should have correct planet IDs matching Swiss Ephemeris`() {
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
    }

    @Test
    fun `should have mean node`() {
        assertThat(Planet.MEAN_NODE.id).isEqualTo(10)
    }

    @Test
    fun `should have true node`() {
        assertThat(Planet.TRUE_NODE.id).isEqualTo(11)
    }

    @Test
    fun `should find planet by ID`() {
        assertThat(Planet.fromId(0)).isEqualTo(Planet.SUN)
        assertThat(Planet.fromId(4)).isEqualTo(Planet.MARS)
        assertThat(Planet.fromId(9)).isEqualTo(Planet.PLUTO)
    }

    @Test
    fun `should return null for invalid planet ID`() {
        assertThat(Planet.fromId(999)).isNull()
        assertThat(Planet.fromId(-1)).isNull()
    }

    @Test
    fun `should have display names`() {
        assertThat(Planet.SUN.displayName).isEqualTo("Sun")
        assertThat(Planet.MARS.displayName).isEqualTo("Mars")
        assertThat(Planet.MEAN_NODE.displayName).isEqualTo("mean Node")
    }

    @Test
    fun `should identify classical planets`() {
        assertThat(Planet.SUN.isClassical).isTrue()
        assertThat(Planet.MOON.isClassical).isTrue()
        assertThat(Planet.MERCURY.isClassical).isTrue()
        assertThat(Planet.VENUS.isClassical).isTrue()
        assertThat(Planet.MARS.isClassical).isTrue()
        assertThat(Planet.JUPITER.isClassical).isTrue()
        assertThat(Planet.SATURN.isClassical).isTrue()
        
        assertThat(Planet.URANUS.isClassical).isFalse()
        assertThat(Planet.NEPTUNE.isClassical).isFalse()
        assertThat(Planet.PLUTO.isClassical).isFalse()
    }

    @Test
    fun `should identify modern planets`() {
        assertThat(Planet.URANUS.isModern).isTrue()
        assertThat(Planet.NEPTUNE.isModern).isTrue()
        assertThat(Planet.PLUTO.isModern).isTrue()
        
        assertThat(Planet.SUN.isModern).isFalse()
        assertThat(Planet.SATURN.isModern).isFalse()
    }

    @Test
    fun `should identify nodes`() {
        assertThat(Planet.MEAN_NODE.isNode).isTrue()
        assertThat(Planet.TRUE_NODE.isNode).isTrue()
        
        assertThat(Planet.SUN.isNode).isFalse()
        assertThat(Planet.MARS.isNode).isFalse()
    }

    @Test
    fun `should get all main planets`() {
        val mainPlanets = Planet.mainPlanets()
        
        assertThat(mainPlanets).containsExactly(
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

    @Test
    fun `should get classical planets only`() {
        val classical = Planet.classicalPlanets()
        
        assertThat(classical).containsExactly(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN
        )
    }
}
