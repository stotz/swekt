package ch.typedef.swekt.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TDD Tests for DataSource enum
 */
class DataSourceTest {

    @Test
    fun `should have EPHEMERIS source`() {
        val source = DataSource.EPHEMERIS
        
        assertThat(source.fileExtension).isEqualTo(".se1")
        assertThat(source.description).contains("Swiss Ephemeris")
    }

    @Test
    fun `should have MOSHIER source`() {
        val source = DataSource.MOSHIER
        
        assertThat(source.fileExtension).isNull()
        assertThat(source.description).contains("Moshier")
        assertThat(source.requiresFiles).isFalse()
    }

    @Test
    fun `should have JPL source`() {
        val source = DataSource.JPL
        
        assertThat(source.fileExtension).isEqualTo(".eph")
        assertThat(source.description).contains("JPL")
    }

    @Test
    fun `should indicate if files are required`() {
        assertThat(DataSource.EPHEMERIS.requiresFiles).isTrue()
        assertThat(DataSource.MOSHIER.requiresFiles).isFalse()
        assertThat(DataSource.JPL.requiresFiles).isTrue()
    }

    @Test
    fun `should have proper display names`() {
        assertThat(DataSource.EPHEMERIS.displayName).isEqualTo("Swiss Ephemeris")
        assertThat(DataSource.MOSHIER.displayName).isEqualTo("Moshier")
        assertThat(DataSource.JPL.displayName).isEqualTo("JPL Ephemeris")
    }

    @Test
    fun `should support lookup by name`() {
        val source = DataSource.fromName("ephemeris")
        
        assertThat(source).isEqualTo(DataSource.EPHEMERIS)
    }

    @Test
    fun `should be case insensitive for lookup`() {
        assertThat(DataSource.fromName("EPHEMERIS")).isEqualTo(DataSource.EPHEMERIS)
        assertThat(DataSource.fromName("ephemeris")).isEqualTo(DataSource.EPHEMERIS)
        assertThat(DataSource.fromName("Ephemeris")).isEqualTo(DataSource.EPHEMERIS)
    }

    @Test
    fun `should return null for unknown source name`() {
        val source = DataSource.fromName("unknown")
        
        assertThat(source).isNull()
    }

    @Test
    fun `should list all available sources`() {
        val sources = DataSource.entries
        
        assertThat(sources).hasSize(3)
        assertThat(sources).contains(
            DataSource.EPHEMERIS,
            DataSource.MOSHIER,
            DataSource.JPL
        )
    }
}
