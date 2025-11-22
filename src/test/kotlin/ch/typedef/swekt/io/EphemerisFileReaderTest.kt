package ch.typedef.swekt.io

import ch.typedef.swekt.config.EphemerisConfig
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

class EphemerisFileReaderTest {

    @Test
    fun `should create reader with config`() {
        val config = EphemerisConfig.builder()
            .addPath("/tmp/ephe")
            .build()
        
        val reader = EphemerisFileReader(config)
        
        assertThat(reader).isNotNull
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should find ephemeris file for planet`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        
        // Should be able to read header for Mars (planet file sepl_*.se1)
        val header = reader.readHeader(Planet.MARS)
        
        assertThat(header).isNotNull
        assertThat(header.fileFormat).isEqualTo(FileFormat.SE1)
        assertThat(header.startJD).isGreaterThan(0.0)
        assertThat(header.endJD).isGreaterThan(header.startJD)
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should find ephemeris file for Moon`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        
        // Moon uses semo_*.se1 files
        val header = reader.readHeader(Planet.MOON)
        
        assertThat(header).isNotNull
        assertThat(header.fileFormat).isEqualTo(FileFormat.SE1)
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should read record for planet at specific time`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val jd = JulianDay.J2000
        
        val record = reader.readRecord(Planet.MARS, jd)
        
        assertThat(record).isNotNull
        assertThat(record.julianDay).isEqualTo(jd)
        assertThat(record.position).hasSize(3)
        assertThat(record.velocity).hasSize(3)
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should reject reading outside time range`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val jd = JulianDay(-1000000.0) // Very old date, likely outside range
        
        assertThrows<IllegalArgumentException> {
            reader.readRecord(Planet.MARS, jd)
        }
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should check availability for planet and time`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        
        // J2000 should be available
        assertThat(reader.isAvailable(Planet.MARS, JulianDay.J2000)).isTrue()
        
        // Very old date likely not available
        assertThat(reader.isAvailable(Planet.MARS, JulianDay(-1000000.0))).isFalse()
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    fun `should cache headers for performance`() {
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        
        // Read header twice
        val header1 = reader.readHeader(Planet.MARS)
        val header2 = reader.readHeader(Planet.MARS)
        
        // Should be same instance (cached)
        assertThat(header1).isSameAs(header2)
    }

    @Test
    fun `should throw exception when file not found`() {
        val config = EphemerisConfig.builder()
            .addPath("/nonexistent/path")
            .build()
        val reader = EphemerisFileReader(config)
        
        assertThrows<IllegalArgumentException> {
            reader.readHeader(Planet.MARS)
        }
    }
}
