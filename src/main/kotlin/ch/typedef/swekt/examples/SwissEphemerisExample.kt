package ch.typedef.swekt.examples

import ch.typedef.swekt.calculation.SwissEphemerisEngine
import ch.typedef.swekt.io.Se1Record
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Demonstrates Swiss Ephemeris Engine with Chebyshev interpolation.
 *
 * Shows how SE1 records are used to calculate high-precision
 * planetary positions.
 */
fun main() {
    println("=== Swiss Ephemeris Engine Demo ===\n")

    // Create a simulated SE1 record (normally read from .se1 file)
    println("1. Creating SE1 Record:")
    val startJd = JulianDay(2451545.0)  // J2000.0
    val endJd = JulianDay(2451577.0)    // 32 days later
    
    val record = Se1Record(
        startJulianDay = startJd,
        endJulianDay = endJd,
        // Mars longitude coefficients (degrees)
        longitudeCoefficients = doubleArrayOf(
            355.4523,   // Mean longitude
            16.2134,    // Linear term (motion)
            -0.0234,    // Quadratic correction
            0.0012,     // Cubic correction
            -0.0001     // Higher order
        ),
        // Mars latitude coefficients (degrees)
        latitudeCoefficients = doubleArrayOf(
            1.8456,     // Mean latitude
            0.1234,     // Linear term
            -0.0023     // Quadratic
        ),
        // Mars distance coefficients (AU)
        distanceCoefficients = doubleArrayOf(
            1.5237,     // Mean distance
            0.0024,     // Linear term
            -0.00001    // Quadratic
        )
    )
    
    println("   Time range: ${startJd.value} to ${endJd.value}")
    println("   Span: ${String.format("%.1f", record.timeSpan)} days")
    println("   Coefficients: ${record.longitudeCoefficients.size}, " +
            "${record.latitudeCoefficients.size}, ${record.distanceCoefficients.size}")
    println()

    // Create engine
    println("2. Calculating Mars positions:\n")
    val engine = SwissEphemerisEngine()
    
    // Calculate at several points
    println("   Julian Day    Date         Longitude  Latitude  Distance   Speed")
    println("   ------------- ------------ ---------- --------- ---------- -------")
    
    for (offset in 0..32 step 8) {
        val jd = JulianDay(startJd.value + offset)
        
        // This is where the magic happens!
        // Chebyshev interpolation calculates position from coefficients
        val position = engine.calculateFromRecord(Planet.MARS, jd, record)
        
        val gregorian = jd.toGregorian()
        val dateStr = "%04d-%02d-%02d".format(
            gregorian.year, 
            gregorian.month, 
            gregorian.day
        )
        
        println("   %.1f  %s  %7.3f°  %6.3f°  %.6f AU  %.4f°/d".format(
            jd.value,
            dateStr,
            position.longitude,
            position.latitude,
            position.distance,
            position.longitudeSpeed
        ))
    }
    println()

    // Compare start vs end
    println("3. Motion Analysis:")
    val posStart = engine.calculateFromRecord(Planet.MARS, startJd, record)
    val posEnd = engine.calculateFromRecord(Planet.MARS, endJd, record)
    
    val lonChange = posEnd.longitude - posStart.longitude
    val distChange = posEnd.distance - posStart.distance
    
    println("   Longitude change: ${String.format("%.3f", lonChange)}° over 32 days")
    println("   Average speed: ${String.format("%.4f", lonChange / 32.0)}°/day")
    println("   Distance change: ${String.format("%.6f", distChange)} AU")
    println()

    // Show Chebyshev interpolation at work
    println("4. Interpolation at different times:")
    
    for (fraction in listOf(0.0, 0.25, 0.5, 0.75, 1.0)) {
        val jd = JulianDay(startJd.value + fraction * record.timeSpan)
        val pos = engine.calculateFromRecord(Planet.MARS, jd, record)
        
        println("   At ${(fraction * 100).toInt()}% through interval: " +
                "lon=${String.format("%.3f", pos.longitude)}°, " +
                "speed=${String.format("%.4f", pos.longitudeSpeed)}°/day")
    }
    println()

    // Demonstrate velocity calculation
    println("5. Velocity Details (at midpoint):")
    val midJd = record.midpoint
    val midPos = engine.calculateFromRecord(Planet.MARS, midJd, record)
    
    println("   Julian Day: ${midJd.value}")
    println("   Longitude:  ${String.format("%.6f", midPos.longitude)}°")
    println("   Longitude Speed: ${String.format("%.6f", midPos.longitudeSpeed)}°/day")
    println("   Latitude:   ${String.format("%.6f", midPos.latitude)}°")
    println("   Latitude Speed:  ${String.format("%.6f", midPos.latitudeSpeed)}°/day")
    println("   Distance:   ${String.format("%.8f", midPos.distance)} AU")
    println("   Distance Speed:  ${String.format("%.8f", midPos.distanceSpeed)} AU/day")
    println()

    println("=== Done ===")
    println()
    println("This is how Swiss Ephemeris calculates planetary positions!")
    println("Production implementation would:")
    println("  1. Read SE1 files from disk")
    println("  2. Find the right record for requested Julian Day")
    println("  3. Use Chebyshev interpolation (as shown above)")
    println("  4. Apply coordinate transformations as needed")
}
