package ch.typedef.swekt.examples

import ch.typedef.swekt.calculation.SwissEphemerisEngine
import ch.typedef.swekt.config.EphemerisConfig
import ch.typedef.swekt.io.EphemerisFileReader
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Example: High-precision planetary calculations using Swiss Ephemeris SE1 files.
 *
 * This example demonstrates the complete workflow:
 * 1. Configure ephemeris path
 * 2. Read SE1 binary data (Chebyshev coefficients)
 * 3. Calculate high-precision positions
 *
 * Requirements:
 * - SE_EPHE_PATH environment variable set
 * - Swiss Ephemeris SE1 files installed
 */
fun main() {
    println("Swiss Ephemeris SE1 Calculation Example")
    println("=".repeat(50))
    println()

    // Check if ephemeris files are available
    val ephePath = System.getenv("SE_EPHE_PATH")
    if (ephePath == null) {
        println("ERROR: SE_EPHE_PATH environment variable not set")
        println("Please set it to your Swiss Ephemeris data directory")
        println("Example: export SE_EPHE_PATH=/usr/share/swisseph")
        return
    }

    try {
        // 1. Configure
        println("Configuration:")
        println("  Ephemeris Path: $ephePath")
        val config = EphemerisConfig.fromEnvironment()
        val reader = EphemerisFileReader(config)
        val engine = SwissEphemerisEngine()
        println()

        // 2. Calculate positions at J2000.0
        val jd = JulianDay.J2000
        val gregorian = jd.toGregorian()
        println("Calculating positions for:")
        println("  Julian Day: ${jd.value}")
        println("  Date: ${gregorian.year}-${gregorian.month.toString().padStart(2, '0')}-${gregorian.day.toString().padStart(2, '0')}")
        println()

        // 3. Calculate for each planet
        val planets = listOf(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN
        )

        println("Planetary Positions (Heliocentric Ecliptic):")
        println("-".repeat(50))
        println("Planet      Longitude    Latitude     Distance")
        println("-".repeat(50))

        for (planet in planets) {
            try {
                val record = reader.readSe1Record(planet, jd)
                val position = engine.calculateFromRecord(planet, jd, record)

                println(
                    "%-10s  %9.4f°  %9.4f°  %9.6f AU".format(
                        planet.displayName,
                        position.longitude,
                        position.latitude,
                        position.distance
                    )
                )
            } catch (e: Exception) {
                println("%-10s  ERROR: ${e.message}".format(planet.displayName))
            }
        }

        println("-".repeat(50))
        println()

        // 4. Detailed calculation for Mars
        println("Detailed Mars Calculation:")
        println("-".repeat(50))
        val mars = Planet.MARS
        val marsRecord = reader.readSe1Record(mars, jd)
        val marsPos = engine.calculateFromRecord(mars, jd, marsRecord)

        println("SE1 Record Information:")
        println("  Time Range: ${marsRecord.startJulianDay.value} to ${marsRecord.endJulianDay.value}")
        println("  Time Span: %.1f days".format(marsRecord.timeSpan))
        println("  Coefficients: ${marsRecord.longitudeCoefficients.size} per coordinate")
        println()

        println("Position:")
        println("  Longitude: %.6f°".format(marsPos.longitude))
        println("  Latitude:  %.6f°".format(marsPos.latitude))
        println("  Distance:  %.8f AU".format(marsPos.distance))
        println()

        println("Velocity:")
        println("  Longitude Speed: %.8f°/day".format(marsPos.longitudeSpeed))
        println("  Latitude Speed:  %.8f°/day".format(marsPos.latitudeSpeed))
        println("  Distance Speed:  %.8f AU/day".format(marsPos.distanceSpeed))
        println()

        println("Calculation complete!")

    } catch (e: Exception) {
        println("ERROR: ${e.message}")
        e.printStackTrace()
    }
}
