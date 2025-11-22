package ch.typedef.swekt.examples

import ch.typedef.swekt.coordinates.*
import ch.typedef.swekt.engine.JplCalculationEngine
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import java.nio.file.Paths

/**
 * Example demonstrating coordinate system transformations.
 *
 * Shows how to:
 * 1. Calculate planetary positions (Cartesian)
 * 2. Convert to ecliptic coordinates (astrology)
 * 3. Convert to equatorial coordinates (astronomy)
 * 4. Convert to horizontal coordinates (observer view)
 */
fun main() {
    println("=".repeat(70))
    println("Coordinate Transformations Example")
    println("=".repeat(70))
    println()

    // Setup JPL engine
    val ephemerisPath = Paths.get(System.getenv("SE_EPHE_PATH") ?: "/c/data/swisseph", "de441.eph")
    
    if (!ephemerisPath.toFile().exists()) {
        println("JPL ephemeris file not found: $ephemerisPath")
        println("Set SE_EPHE_PATH environment variable or place de441.eph in /c/data/swisseph")
        return
    }

    val engine = JplCalculationEngine(ephemerisPath)
    val now = JulianDay.J2000  // Using J2000.0 epoch for demonstration

    println("Time: ${now.toGregorian()}")
    println("Julian Day: ${now.value}")
    println()

    // Calculate Mars position
    val mars = engine.calculateGeocentricPosition(Planet.MARS, now)
    
    println("--- MARS Position ---")
    println()
    
    // 1. Cartesian Coordinates
    println("1. CARTESIAN (Geocentric)")
    println("   X: ${"%.0f".format(mars.position.x)} km")
    println("   Y: ${"%.0f".format(mars.position.y)} km")
    println("   Z: ${"%.0f".format(mars.position.z)} km")
    println("   Distance: ${"%.0f".format(mars.distance())} km")
    println()

    // 2. Ecliptic Coordinates (Astrology!)
    val ecliptic = mars.toEcliptic()
    println("2. ECLIPTIC (Astrology)")
    println("   Longitude λ: ${"%.4f".format(ecliptic.longitude)}°")
    println("   Latitude β: ${"%.4f".format(ecliptic.latitude)}°")
    println("   Distance: ${"%.0f".format(ecliptic.distance)} km")
    println("   Zodiac Sign: ${ecliptic.toAstrologicalString()}")
    println()

    // 3. Equatorial Coordinates (Astronomy)
    val equatorial = mars.toEquatorial()
    println("3. EQUATORIAL (Astronomy)")
    println("   Right Ascension α: ${"%.4f".format(equatorial.rightAscension)}h")
    println("   Declination δ: ${"%.4f".format(equatorial.declination)}°")
    println("   Distance: ${"%.0f".format(equatorial.distance)} km")
    println("   Formatted: ${equatorial.toAstronomicalString()}")
    println()

    // 4. Horizontal Coordinates (Observer View)
    // Example: Observer in Zürich
    val observerLat = 47.3769  // Zürich latitude
    val observerLon = 8.5417   // Zürich longitude
    
    // For proper LST calculation, we'd need time functions
    // For now, use approximate LST
    val approximateLST = (now.value % 1.0) * 24.0  // Rough approximation
    
    val horizontal = mars.toHorizontal(observerLat, approximateLST)
    println("4. HORIZONTAL (Observer in Zürich)")
    println("   Azimuth: ${"%.2f".format(horizontal.azimuth)}°")
    println("   Altitude: ${"%.2f".format(horizontal.altitude)}°")
    println("   Direction: ${horizontal.cardinalDirection()}")
    println("   Visible: ${if (horizontal.isAboveHorizon()) "Yes (above horizon)" else "No (below horizon)"}")
    if (horizontal.isAboveHorizon()) {
        println("   Air Mass: ${"%.2f".format(horizontal.airMass())}")
    }
    println()

    // Show all planets in ecliptic coordinates
    println("=".repeat(70))
    println("All Planets - Ecliptic Coordinates (Astrological)")
    println("=".repeat(70))
    println()
    
    for (planet in listOf(
        Planet.SUN, Planet.MOON, Planet.MERCURY, Planet.VENUS,
        Planet.MARS, Planet.JUPITER, Planet.SATURN
    )) {
        try {
            val pos = engine.calculateGeocentricPosition(planet, now)
            val ecl = pos.toEcliptic()
            
            println("${planet.displayName.padEnd(10)}: ${ecl.toAstrologicalString()}")
        } catch (e: Exception) {
            println("${planet.displayName.padEnd(10)}: Error - ${e.message}")
        }
    }
    
    println()
    println("=".repeat(70))
    println("Note: Horizontal coordinates require precise sidereal time calculation.")
    println("For accurate results, implement proper time transformations (Phase 5c next step).")
    println("=".repeat(70))
}
