package ch.typedef.swekt.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.nio.file.Paths

/**
 * Integration tests for real Swiss Ephemeris installation.
 * 
 * These tests are only enabled if SE_EPHE_PATH environment variable is set.
 * They verify that the actual installation is correct and accessible.
 */
@EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".*")
class SwissEphemerisInstallationTest {

    private val ephePath = System.getenv("SE_EPHE_PATH")
    
    @Test
    fun `should read SE_EPHE_PATH environment variable`() {
        assertThat(ephePath).isNotNull
        assertThat(ephePath).isNotEmpty()
        println("SE_EPHE_PATH = $ephePath")
    }

    @Test
    fun `should find Swiss Ephemeris directory`() {
        val path = Paths.get(ephePath)
        
        assertThat(path).exists()
        assertThat(path).isDirectory()
    }

    @Test
    fun `should have core planet files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        // Core files that should exist
        assertThat(resolver.exists("sepl_18.se1"))
            .withFailMessage("sepl_18.se1 not found - planets for 1800-1899")
            .isTrue()
        
        assertThat(resolver.exists("semo_18.se1"))
            .withFailMessage("semo_18.se1 not found - moon for 1800-1899")
            .isTrue()
        
        assertThat(resolver.exists("seas_18.se1"))
            .withFailMessage("seas_18.se1 not found - main asteroids for 1800-1899")
            .isTrue()
    }

    @Test
    fun `should have JPL ephemeris files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        val jplFiles = listOf("de200.eph", "de406e.eph", "de431.eph", "de440.eph", "de441.eph")
        val foundJpl = jplFiles.filter { resolver.exists(it) }
        
        assertThat(foundJpl)
            .withFailMessage("No JPL files found. Expected at least one of: $jplFiles")
            .isNotEmpty()
        
        println("Found JPL files: $foundJpl")
    }

    @Test
    fun `should have extended time range files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        // Check for different centuries
        val centuries = listOf("00", "06", "12", "18", "24")
        val foundCenturies = centuries.filter { 
            resolver.exists("sepl_$it.se1") 
        }
        
        assertThat(foundCenturies.size)
            .withFailMessage("Found only ${foundCenturies.size} century files, expected at least 3")
            .isGreaterThanOrEqualTo(3)
        
        println("Found planet files for centuries: $foundCenturies")
    }

    @Test
    fun `should have Saturn moon files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        // Saturn moons are in sat/ subdirectory
        val satMoonFile = resolver.findFile("sat/sepm9401.se1")
        
        if (satMoonFile != null) {
            assertThat(satMoonFile).exists()
            println("Found Saturn moons in: ${satMoonFile.parent}")
        } else {
            println("Saturn moon files not found (optional)")
        }
    }

    @Test
    fun `should list all available planet files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        val planetFiles = resolver.listAvailableFiles("sepl_*.se1")
        
        assertThat(planetFiles)
            .withFailMessage("No planet files found matching sepl_*.se1")
            .isNotEmpty()
        
        println("Found ${planetFiles.size} planet files:")
        planetFiles.take(5).forEach { 
            println("  - ${it.fileName}") 
        }
        if (planetFiles.size > 5) {
            println("  ... and ${planetFiles.size - 5} more")
        }
    }

    @Test
    fun `should list all available moon files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        val moonFiles = resolver.listAvailableFiles("semo_*.se1")
        
        assertThat(moonFiles)
            .withFailMessage("No moon files found matching semo_*.se1")
            .isNotEmpty()
        
        println("Found ${moonFiles.size} moon files")
    }

    @Test
    fun `should list all available asteroid files`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        val asteroidFiles = resolver.listAvailableFiles("seas_*.se1")
        
        assertThat(asteroidFiles)
            .withFailMessage("No asteroid files found matching seas_*.se1")
            .isNotEmpty()
        
        println("Found ${asteroidFiles.size} asteroid files")
    }

    @Test
    fun `should have fixstars file`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        val hasFixstars = resolver.exists("sefstars.txt")
        
        if (hasFixstars) {
            println("Fixed stars file found")
        } else {
            println("Fixed stars file not found (optional)")
        }
    }

    @Test
    fun `should report complete installation summary`() {
        val config = EphemerisConfig.fromEnvironment(ephePath)
        val resolver = EphemerisPathResolver(config)
        
        println("\n=== Swiss Ephemeris Installation Summary ===")
        println("Path: $ephePath")
        println()
        
        // Count files
        val planetFiles = resolver.listAvailableFiles("sepl_*.se1").size
        val moonFiles = resolver.listAvailableFiles("semo_*.se1").size
        val asteroidFiles = resolver.listAvailableFiles("seas_*.se1").size
        
        println("Planet files (sepl_*.se1):    $planetFiles")
        println("Moon files (semo_*.se1):      $moonFiles")
        println("Asteroid files (seas_*.se1):  $asteroidFiles")
        println()
        
        // Check JPL
        val jplFiles = listOf("de200.eph", "de406e.eph", "de431.eph", "de440.eph", "de441.eph")
        val foundJpl = jplFiles.filter { resolver.exists(it) }
        println("JPL files: ${foundJpl.joinToString(", ")}")
        println()
        
        // Time coverage estimation
        val centuryPattern = """sepl_(\d+)\.se1""".toRegex()
        val centuries = resolver.listAvailableFiles("sepl_*.se1")
            .mapNotNull { 
                centuryPattern.matchEntire(it.fileName.toString())?.groupValues?.get(1)?.toInt() 
            }
            .sorted()
        
        if (centuries.isNotEmpty()) {
            val minYear = centuries.first() * 100
            val maxYear = (centuries.last() + 1) * 100
            println("Time coverage: $minYear CE to $maxYear CE (${maxYear - minYear} years)")
        }
        
        println("===========================================\n")
    }
}
