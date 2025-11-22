package ch.typedef.swekt.math

import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Chebyshev polynomial interpolation.
 *
 * Chebyshev polynomials are used in Swiss Ephemeris for interpolating
 * planetary positions from coefficient tables.
 *
 * Reference: ACM Algorithm 446 (Communications of the ACM, April 1973)
 * Author: Dr. Roger Broucke
 */
class ChebyshevInterpolationTest {

    @Test
    fun `evaluate simple polynomial T0(x) = 1`() {
        // T_0(x) = 1 for all x
        val coefficients = doubleArrayOf(1.0)
        
        assertEquals(1.0, ChebyshevInterpolation.evaluate(-1.0, coefficients), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.evaluate(0.0, coefficients), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.evaluate(1.0, coefficients), 0.0001)
    }

    @Test
    fun `evaluate T1(x) = x`() {
        // T_1(x) = x
        val coefficients = doubleArrayOf(0.0, 1.0)
        
        assertEquals(-1.0, ChebyshevInterpolation.evaluate(-1.0, coefficients), 0.0001)
        assertEquals(0.0, ChebyshevInterpolation.evaluate(0.0, coefficients), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.evaluate(1.0, coefficients), 0.0001)
        assertEquals(0.5, ChebyshevInterpolation.evaluate(0.5, coefficients), 0.0001)
    }

    @Test
    fun `evaluate T2(x) = 2x² - 1`() {
        // For T_2(x) = 2x² - 1, we need:
        // f(x) = 0*T_0 + 0*T_1 + 1*T_2
        // So coefficients are [0, 0, 1]
        val coefficients = doubleArrayOf(0.0, 0.0, 1.0)
        
        // T_2(-1) = 2*1 - 1 = 1
        assertEquals(1.0, ChebyshevInterpolation.evaluate(-1.0, coefficients), 0.0001)
        // T_2(0) = 0 - 1 = -1
        assertEquals(-1.0, ChebyshevInterpolation.evaluate(0.0, coefficients), 0.0001)
        // T_2(1) = 2*1 - 1 = 1
        assertEquals(1.0, ChebyshevInterpolation.evaluate(1.0, coefficients), 0.0001)
        // T_2(0.5) = 2*0.25 - 1 = -0.5
        assertEquals(-0.5, ChebyshevInterpolation.evaluate(0.5, coefficients), 0.0001)
    }

    @Test
    fun `evaluate cosine approximation`() {
        // cos(π*x) can be approximated with Chebyshev polynomials
        // This test just verifies multi-term evaluation works
        
        val coefficients = doubleArrayOf(
            1.0, 0.0, -0.5, 0.0, 0.04
        )
        
        val x = 0.5
        val result = ChebyshevInterpolation.evaluate(x, coefficients)
        
        // Just verify it computes without error
        assertTrue(result.isFinite(), "Result should be finite")
        assertTrue(abs(result) < 10.0, "Result should be reasonable")
    }

    @Test
    fun `evaluate with multiple coefficients`() {
        // Test with realistic number of coefficients (like SE uses)
        val coefficients = doubleArrayOf(
            1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0
        )
        
        val result = ChebyshevInterpolation.evaluate(0.5, coefficients)
        
        // Just verify it computes without error and gives reasonable result
        assertTrue(result.isFinite(), "Result should be finite")
        assertTrue(abs(result) < 1000.0, "Result should be reasonable magnitude")
    }

    @Test
    fun `evaluate at boundary points`() {
        val coefficients = doubleArrayOf(1.0, 2.0, 3.0)
        
        // Should work at boundaries of [-1, 1]
        val atMinus1 = ChebyshevInterpolation.evaluate(-1.0, coefficients)
        val atPlus1 = ChebyshevInterpolation.evaluate(1.0, coefficients)
        
        assertTrue(atMinus1.isFinite())
        assertTrue(atPlus1.isFinite())
    }

    @Test
    fun `evaluate derivative T0'(x) = 0`() {
        // Derivative of constant is 0
        val coefficients = doubleArrayOf(1.0)
        
        assertEquals(0.0, ChebyshevInterpolation.evaluateDerivative(-1.0, coefficients), 0.0001)
        assertEquals(0.0, ChebyshevInterpolation.evaluateDerivative(0.0, coefficients), 0.0001)
        assertEquals(0.0, ChebyshevInterpolation.evaluateDerivative(1.0, coefficients), 0.0001)
    }

    @Test
    fun `evaluate derivative T1'(x) = 1`() {
        // Derivative of T_1(x) = x is 1
        val coefficients = doubleArrayOf(0.0, 1.0)
        
        assertEquals(1.0, ChebyshevInterpolation.evaluateDerivative(-1.0, coefficients), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.evaluateDerivative(0.0, coefficients), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.evaluateDerivative(1.0, coefficients), 0.0001)
    }

    @Test
    fun `evaluate derivative T2'(x) = 4x`() {
        // Derivative of T_2(x) = 2x² - 1 is 4x
        // Coefficients: [0, 0, 1] for T_2
        val coefficients = doubleArrayOf(0.0, 0.0, 1.0)
        
        assertEquals(-4.0, ChebyshevInterpolation.evaluateDerivative(-1.0, coefficients), 0.0001)
        assertEquals(0.0, ChebyshevInterpolation.evaluateDerivative(0.0, coefficients), 0.0001)
        assertEquals(4.0, ChebyshevInterpolation.evaluateDerivative(1.0, coefficients), 0.0001)
        assertEquals(2.0, ChebyshevInterpolation.evaluateDerivative(0.5, coefficients), 0.0001)
    }

    @Test
    fun `derivative of sine approximation`() {
        // If f(x) ≈ sin(πx), then f'(x) ≈ π*cos(πx)
        val coefficients = doubleArrayOf(
            0.0, 1.2337, 0.0, -0.2533
        )
        
        val x = 0.5
        val derivative = ChebyshevInterpolation.evaluateDerivative(x, coefficients)
        
        // Derivative should be finite and reasonable
        assertTrue(derivative.isFinite())
        assertTrue(abs(derivative) < 10.0)
    }

    @Test
    fun `consistency between function and derivative`() {
        // For small h, f'(x) ≈ (f(x+h) - f(x-h)) / (2h)
        val coefficients = doubleArrayOf(1.0, 2.0, 3.0, 4.0)
        
        val x = 0.3
        val h = 0.001
        
        val fPlus = ChebyshevInterpolation.evaluate(x + h, coefficients)
        val fMinus = ChebyshevInterpolation.evaluate(x - h, coefficients)
        val numericalDerivative = (fPlus - fMinus) / (2 * h)
        
        val analyticalDerivative = ChebyshevInterpolation.evaluateDerivative(x, coefficients)
        
        assertEquals(numericalDerivative, analyticalDerivative, 0.01,
            "Analytical derivative should match numerical derivative")
    }

    @Test
    fun `empty coefficients array throws exception`() {
        val coefficients = doubleArrayOf()
        
        try {
            ChebyshevInterpolation.evaluate(0.0, coefficients)
            throw AssertionError("Should have thrown exception for empty array")
        } catch (e: IllegalArgumentException) {
            // Expected
        }
    }

    @Test
    fun `derivative with single coefficient`() {
        // Derivative of constant function
        val coefficients = doubleArrayOf(5.0)
        
        val derivative = ChebyshevInterpolation.evaluateDerivative(0.5, coefficients)
        assertEquals(0.0, derivative, 0.0001)
    }

    @Test
    fun `real world coefficients from Swiss Ephemeris`() {
        // Example coefficients - Chebyshev series
        val coefficients = doubleArrayOf(
            1.523,     // T_0 term
            0.00024,   // T_1 term  
            -0.00001,  // T_2 term
            0.000001   // T_3 term
        )
        
        // Evaluate at various points
        val posCenter = ChebyshevInterpolation.evaluate(0.0, coefficients)
        val posEdge = ChebyshevInterpolation.evaluate(1.0, coefficients)
        
        // Should be finite and reasonable
        assertTrue(posCenter.isFinite())
        assertTrue(posEdge.isFinite())
        assertTrue(abs(posCenter) < 10.0)
        
        // Evaluate velocity (useful for planetary motion)
        val velocity = ChebyshevInterpolation.evaluateDerivative(0.0, coefficients)
        
        assertTrue(velocity.isFinite())
    }
}
