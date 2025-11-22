package ch.typedef.swekt.io

import ch.typedef.swekt.config.EphemerisConfig
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Reads ephemeris data from Swiss Ephemeris binary files.
 *
 * Supports reading planetary positions and velocities from .se1 files.
 *
 * @property config Ephemeris configuration
 */
class EphemerisFileReader(private val config: EphemerisConfig) {

    private val cache = mutableMapOf<String, EphemerisFileHeader>()

    /**
     * Reads header from ephemeris file.
     *
     * @param planet Planet to read header for
     * @return File header
     * @throws IllegalArgumentException if file not found
     */
    fun readHeader(planet: Planet): EphemerisFileHeader {
        val file = findFile(planet) ?: throw IllegalArgumentException(
            "No ephemeris file found for planet ${planet.displayName}"
        )
        
        val cacheKey = file.toString()
        return cache.getOrPut(cacheKey) {
            EphemerisFileHeader.read(file)
        }
    }

    /**
     * Reads ephemeris record for planet at given time.
     *
     * @param planet Planet to read
     * @param julianDay Time
     * @return Ephemeris record with position and velocity
     * @throws IllegalArgumentException if no data available
     */
    fun readRecord(planet: Planet, julianDay: JulianDay): EphemerisRecord {
        val file = findFile(planet) ?: throw IllegalArgumentException(
            "No ephemeris file found for planet ${planet.displayName}"
        )
        
        val header = readHeader(planet)
        
        // Validate time range
        require(julianDay.value >= header.startJD && julianDay.value <= header.endJD) {
            "Julian Day ${julianDay.value} outside file range ${header.startJD} - ${header.endJD}"
        }
        
        // For now, return dummy data
        // TODO: Implement actual binary reading
        return EphemerisRecord(
            julianDay = julianDay,
            position = doubleArrayOf(0.0, 0.0, 0.0),
            velocity = doubleArrayOf(0.0, 0.0, 0.0)
        )
    }

    /**
     * Reads SE1 record for planet at given time.
     *
     * This is the new high-precision method using Chebyshev coefficients.
     *
     * @param planet Planet to read
     * @param julianDay Time
     * @return SE1 record with Chebyshev coefficients
     * @throws IllegalArgumentException if no data available
     */
    fun readSe1Record(planet: Planet, julianDay: JulianDay): Se1Record {
        val file = findFile(planet) ?: throw IllegalArgumentException(
            "No ephemeris file found for planet ${planet.displayName}"
        )
        
        val header = readHeader(planet)
        
        // Validate time range
        require(julianDay.value >= header.startJD && julianDay.value <= header.endJD) {
            "Julian Day ${julianDay.value} outside file range ${header.startJD} - ${header.endJD}"
        }
        
        // Read using binary reader
        val reader = Se1BinaryReader(file)
        return reader.findRecord(julianDay) ?: throw IllegalArgumentException(
            "No SE1 record found for Julian Day ${julianDay.value} in file $file"
        )
    }

    /**
     * Finds ephemeris file for planet.
     *
     * @param planet Planet
     * @return File path or null if not found
     */
    private fun findFile(planet: Planet): Path? {
        val searchPaths = config.searchPaths
        val planetId = planet.id
        
        for (pathStr in searchPaths) {
            // Create path - Paths.get handles both absolute and relative correctly
            val basePath = try {
                Paths.get(pathStr).toAbsolutePath()
            } catch (e: Exception) {
                // Skip invalid paths
                continue
            }
            
            // Skip if base path doesn't exist
            if (!Files.exists(basePath) || !Files.isDirectory(basePath)) {
                continue
            }
            
            // Try sepl_*.se1 (planets), semo_*.se1 (moon), seas_*.se1 (asteroids)
            val prefix = when {
                planetId == 1 -> "semo" // Moon
                planetId < 10 -> "sepl" // Planets
                else -> null
            }
            
            if (prefix != null) {
                // Try different centuries
                for (century in 0..162) {
                    val centuryStr = century.toString().padStart(2, '0')
                    val filename = "${prefix}_${centuryStr}.se1"
                    val file = basePath.resolve(filename)
                    
                    if (Files.exists(file) && Files.isRegularFile(file)) {
                        return file
                    }
                }
            }
        }
        
        return null
    }

    /**
     * Checks if ephemeris data is available for planet at given time.
     *
     * @param planet Planet
     * @param julianDay Time
     * @return True if data available
     */
    fun isAvailable(planet: Planet, julianDay: JulianDay): Boolean {
        return try {
            val header = readHeader(planet)
            julianDay.value >= header.startJD && julianDay.value <= header.endJD
        } catch (e: Exception) {
            false
        }
    }
}
