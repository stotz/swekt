package ch.typedef.swekt.math

/**
 * Chebyshev polynomial interpolation.
 *
 * Chebyshev polynomials are used in Swiss Ephemeris for efficient
 * interpolation of planetary positions from stored coefficient tables.
 *
 * This implementation uses Clenshaw's algorithm (ACM Algorithm 446)
 * for stable evaluation of Chebyshev series.
 *
 * Reference:
 * - ACM Algorithm 446, Communications of the ACM, Vol. 16, No. 4, April 1973
 * - Author: Dr. Roger Broucke
 * - Swiss Ephemeris: swephlib.c, functions swi_echeb() and swi_edcheb()
 *
 * Chebyshev polynomials T_n(x) are defined on [-1, 1]:
 * - T_0(x) = 1
 * - T_1(x) = x
 * - T_2(x) = 2x² - 1
 * - T_n(x) = 2x*T_(n-1)(x) - T_(n-2)(x)
 *
 * Or equivalently: T_n(cos(θ)) = cos(n*θ)
 */
object ChebyshevInterpolation {

    /**
     * Evaluates a Chebyshev series at a given point.
     *
     * Computes: f(x) = Σ c_i * T_i(x) where T_i are Chebyshev polynomials.
     *
     * Uses Clenshaw's recurrence algorithm for numerical stability:
     * - More stable than direct evaluation
     * - Only requires O(n) operations
     * - Works well for all x in [-1, 1]
     *
     * @param x Point at which to evaluate (must be in [-1, 1])
     * @param coefficients Chebyshev coefficients [c_0, c_1, c_2, ..., c_n]
     * @return Value of the Chebyshev series at x
     * @throws IllegalArgumentException if coefficients array is empty
     */
    fun evaluate(x: Double, coefficients: DoubleArray): Double {
        require(coefficients.isNotEmpty()) {
            "Coefficients array must not be empty"
        }

        if (coefficients.size == 1) {
            return coefficients[0]
        }

        // Clenshaw's algorithm
        val x2 = x * 2.0
        var br = 0.0      // b_r
        var brp2 = 0.0    // b_(r+2)
        var brpp = 0.0    // b_(r+1)

        // Backward recurrence: iterate from highest to lowest degree
        for (j in coefficients.size - 1 downTo 0) {
            brp2 = brpp
            brpp = br
            br = x2 * brpp - brp2 + coefficients[j]
        }

        // Final step: (b_0 - b_2) / 2
        return (br - brp2) * 0.5
    }

    /**
     * Evaluates the derivative of a Chebyshev series.
     *
     * Computes: f'(x) = Σ c_i * T'_i(x)
     *
     * This is useful for calculating velocities from position polynomials:
     * - Position: p(t) = Σ c_i * T_i(t)
     * - Velocity: v(t) = p'(t) = Σ c_i * T'_i(t)
     *
     * Uses modified Clenshaw algorithm for derivatives.
     *
     * @param x Point at which to evaluate derivative (must be in [-1, 1])
     * @param coefficients Chebyshev coefficients [c_0, c_1, c_2, ..., c_n]
     * @return Value of the derivative at x
     * @throws IllegalArgumentException if coefficients array is empty
     */
    fun evaluateDerivative(x: Double, coefficients: DoubleArray): Double {
        require(coefficients.isNotEmpty()) {
            "Coefficients array must not be empty"
        }

        if (coefficients.size == 1) {
            // Derivative of constant is 0
            return 0.0
        }

        // Modified Clenshaw algorithm for derivatives
        val x2 = x * 2.0
        var bf = 0.0      // Final value (dummy init)
        var bj = 0.0      // Current b value
        var bjp2 = 0.0    // b_(j+2)
        var bjpl = 0.0    // b_(j+1)
        var xjp2 = 0.0    // x_(j+2)
        var xjpl = 0.0    // x_(j+1)

        // Backward recurrence starting from j = n-1 down to j = 1
        for (j in coefficients.size - 1 downTo 1) {
            val dj = (j + j).toDouble()  // 2*j
            val xj = coefficients[j] * dj + xjp2
            bj = x2 * bjpl - bjp2 + xj
            bf = bjp2
            bjp2 = bjpl
            bjpl = bj
            xjp2 = xjpl
            xjpl = xj
        }

        // Final step
        return (bj - bf) * 0.5
    }

    /**
     * Convenience function to evaluate both function and derivative.
     *
     * More efficient than calling evaluate() and evaluateDerivative() separately
     * when both values are needed (e.g., position and velocity).
     *
     * @param x Point at which to evaluate
     * @param coefficients Chebyshev coefficients
     * @return Pair of (value, derivative) at x
     */
    fun evaluateBoth(x: Double, coefficients: DoubleArray): Pair<Double, Double> {
        val value = evaluate(x, coefficients)
        val derivative = evaluateDerivative(x, coefficients)
        return Pair(value, derivative)
    }

    /**
     * Normalizes a value from [a, b] to [-1, 1] for Chebyshev evaluation.
     *
     * Chebyshev polynomials are defined on [-1, 1], but data often covers
     * different ranges. This function maps from the data range to [-1, 1].
     *
     * Example:
     * - Julian days 2451545.0 to 2451645.0 (100 days)
     * - Day 2451595.0 (middle) maps to x = 0.0
     * - Day 2451545.0 (start) maps to x = -1.0
     * - Day 2451645.0 (end) maps to x = 1.0
     *
     * @param value Value in original range [a, b]
     * @param a Start of range
     * @param b End of range
     * @return Normalized value in [-1, 1]
     */
    fun normalize(value: Double, a: Double, b: Double): Double {
        require(b > a) { "Range end must be greater than start: [$a, $b]" }
        require(value in a..b) { "Value $value must be in range [$a, $b]" }
        
        // Map [a, b] to [-1, 1]
        // x = -1 + 2 * (value - a) / (b - a)
        return -1.0 + 2.0 * (value - a) / (b - a)
    }

    /**
     * Denormalizes a value from [-1, 1] to [a, b].
     *
     * Inverse of normalize(). Useful for converting Chebyshev coordinates
     * back to the original data range.
     *
     * @param x Normalized value in [-1, 1]
     * @param a Start of range
     * @param b End of range
     * @return Value in original range [a, b]
     */
    fun denormalize(x: Double, a: Double, b: Double): Double {
        require(b > a) { "Range end must be greater than start: [$a, $b]" }
        require(x in -1.0..1.0) { "Normalized value must be in [-1, 1]: $x" }
        
        // Map [-1, 1] to [a, b]
        // value = a + (b - a) * (x + 1) / 2
        return a + (b - a) * (x + 1.0) / 2.0
    }
}
