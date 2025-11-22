package ch.typedef.swekt.examples

import ch.typedef.swekt.math.ChebyshevInterpolation
import ch.typedef.swekt.model.JulianDay
import kotlin.math.PI
import kotlin.math.cos

/**
 * Demonstrates Chebyshev polynomial interpolation.
 *
 * Shows how Swiss Ephemeris uses Chebyshev polynomials to
 * interpolate planetary positions efficiently.
 */
fun main() {
    println("=== Chebyshev Interpolation Demo ===\n")

    // Example 1: Simple polynomial evaluation
    println("1. Basic Chebyshev Polynomials:")
    
    val t0 = doubleArrayOf(1.0)  // T_0(x) = 1
    val t1 = doubleArrayOf(0.0, 1.0)  // T_1(x) = x
    val t2 = doubleArrayOf(-1.0, 0.0, 2.0)  // T_2(x) = 2x² - 1
    
    println("   T_0(0.5) = ${ChebyshevInterpolation.evaluate(0.5, t0)}")
    println("   T_1(0.5) = ${ChebyshevInterpolation.evaluate(0.5, t1)}")
    println("   T_2(0.5) = ${ChebyshevInterpolation.evaluate(0.5, t2)}")
    println()

    // Example 2: Approximate cosine function
    println("2. Approximating cos(πx) with Chebyshev:")
    
    // cos(πx) ≈ sum of Chebyshev terms
    val cosineCoeffs = doubleArrayOf(
        -0.05, 0.0, -1.2337, 0.0, 0.2533
    )
    
    for (x in listOf(-1.0, -0.5, 0.0, 0.5, 1.0)) {
        val actual = cos(PI * x)
        val approx = ChebyshevInterpolation.evaluate(x, cosineCoeffs)
        val error = actual - approx
        println("   x=%.1f: cos(πx)=%.4f, approx=%.4f, error=%.4f".format(x, actual, approx, error))
    }
    println()

    // Example 3: Simulated planetary position
    println("3. Simulated Mars Position (like Swiss Ephemeris):")
    
    // Coefficients representing Mars position over 32 days
    // These would come from an SE1 file
    val marsPositionCoeffs = doubleArrayOf(
        355.4523,   // Mean longitude (degrees)
        16.2134,    // Linear term (motion per normalized day)
        -0.0234,    // Quadratic correction
        0.0012,     // Cubic correction
        -0.0001     // Higher order
    )
    
    // Time range: J2000.0 + 32 days
    val startJd = 2451545.0  // J2000.0
    val endJd = 2451577.0    // 32 days later
    
    // Calculate position every 8 days
    println("   Julian Day    Date         Longitude  Speed")
    println("   ------------- ------------ ---------- --------")
    
    for (offset in 0..32 step 8) {
        val jd = startJd + offset
        val normalizedTime = ChebyshevInterpolation.normalize(jd, startJd, endJd)
        
        // Evaluate position and velocity
        val (position, velocity) = ChebyshevInterpolation.evaluateBoth(
            normalizedTime, 
            marsPositionCoeffs
        )
        
        // Convert velocity from per-normalized-day to per-Julian-day
        val velocityPerDay = velocity / (endJd - startJd) * 2.0
        
        val gregorian = JulianDay(jd).toGregorian()
        val dateStr = "%04d-%02d-%02d".format(gregorian.year, gregorian.month, gregorian.day)
        
        println("   %.1f  %s  %7.3f°   %.4f°/day".format(
            jd, dateStr, position, velocityPerDay
        ))
    }
    println()

    // Example 4: Coordinate normalization
    println("4. Julian Day Normalization:")
    
    val testJd = 2451561.0  // Middle of range
    val normalized = ChebyshevInterpolation.normalize(testJd, startJd, endJd)
    val recovered = ChebyshevInterpolation.denormalize(normalized, startJd, endJd)
    
    println("   Original JD:    $testJd")
    println("   Normalized:     $normalized")
    println("   Recovered:      $recovered")
    println("   Error:          ${testJd - recovered}")
    println()

    // Example 5: Derivative (velocity) calculation
    println("5. Position and Velocity from same coefficients:")
    
    val coeffs = doubleArrayOf(1.523, 0.00024, -0.00001)  // Mars distance (AU)
    
    val x = 0.3
    val position = ChebyshevInterpolation.evaluate(x, coeffs)
    val velocity = ChebyshevInterpolation.evaluateDerivative(x, coeffs)
    
    println("   At x=$x:")
    println("   Position: %.6f AU".format(position))
    println("   Velocity: %.8f AU/normalized-time".format(velocity))
    println()

    println("=== Done ===")
    println()
    println("Note: Chebyshev interpolation is the core algorithm")
    println("used by Swiss Ephemeris for all planetary calculations!")
}
