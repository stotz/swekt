package ch.typedef.swekt.math

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

/**
 * Tests for Chebyshev coordinate normalization.
 *
 * These functions map between data ranges and the Chebyshev [-1, 1] domain.
 */
class ChebyshevNormalizationTest {

    @Test
    fun `normalize maps range endpoints correctly`() {
        val a = 100.0
        val b = 200.0

        assertEquals(-1.0, ChebyshevInterpolation.normalize(a, a, b), 0.0001)
        assertEquals(1.0, ChebyshevInterpolation.normalize(b, a, b), 0.0001)
    }

    @Test
    fun `normalize maps midpoint to zero`() {
        val a = 100.0
        val b = 200.0
        val mid = 150.0

        assertEquals(0.0, ChebyshevInterpolation.normalize(mid, a, b), 0.0001)
    }

    @Test
    fun `normalize works with Julian days`() {
        // Swiss Ephemeris typically stores 32 days per record
        val startJd = 2451545.0  // J2000.0
        val endJd = 2451577.0    // 32 days later

        // Start of range
        assertEquals(-1.0, ChebyshevInterpolation.normalize(startJd, startJd, endJd), 0.0001)
        
        // End of range
        assertEquals(1.0, ChebyshevInterpolation.normalize(endJd, startJd, endJd), 0.0001)
        
        // Middle (16 days after start)
        val midJd = 2451561.0
        assertEquals(0.0, ChebyshevInterpolation.normalize(midJd, startJd, endJd), 0.0001)
        
        // Quarter point (8 days after start)
        val quarterJd = 2451553.0
        assertEquals(-0.5, ChebyshevInterpolation.normalize(quarterJd, startJd, endJd), 0.0001)
    }

    @Test
    fun `normalize throws on invalid range`() {
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.normalize(5.0, 10.0, 5.0)  // b <= a
        }
    }

    @Test
    fun `normalize throws on out of range value`() {
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.normalize(0.0, 10.0, 20.0)  // value < a
        }
        
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.normalize(25.0, 10.0, 20.0)  // value > b
        }
    }

    @Test
    fun `denormalize is inverse of normalize`() {
        val a = 2451545.0
        val b = 2451577.0
        val original = 2451560.0

        val normalized = ChebyshevInterpolation.normalize(original, a, b)
        val denormalized = ChebyshevInterpolation.denormalize(normalized, a, b)

        assertEquals(original, denormalized, 0.0001)
    }

    @Test
    fun `denormalize maps Chebyshev domain correctly`() {
        val a = 100.0
        val b = 200.0

        assertEquals(a, ChebyshevInterpolation.denormalize(-1.0, a, b), 0.0001)
        assertEquals(b, ChebyshevInterpolation.denormalize(1.0, a, b), 0.0001)
        assertEquals(150.0, ChebyshevInterpolation.denormalize(0.0, a, b), 0.0001)
    }

    @Test
    fun `denormalize throws on invalid range`() {
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.denormalize(0.0, 10.0, 5.0)
        }
    }

    @Test
    fun `denormalize throws on out of domain value`() {
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.denormalize(-1.5, 10.0, 20.0)
        }
        
        assertThrows<IllegalArgumentException> {
            ChebyshevInterpolation.denormalize(1.5, 10.0, 20.0)
        }
    }

    @Test
    fun `round trip normalization preserves value`() {
        val ranges = listOf(
            Triple(0.0, 100.0, 50.0),
            Triple(2451545.0, 2451577.0, 2451560.0),
            Triple(-100.0, 100.0, 0.0),
            Triple(1.0, 2.0, 1.5)
        )

        for ((a, b, value) in ranges) {
            val x = ChebyshevInterpolation.normalize(value, a, b)
            val recovered = ChebyshevInterpolation.denormalize(x, a, b)
            assertEquals(value, recovered, 0.0001, "Round trip for value=$value in [$a, $b]")
        }
    }
}
