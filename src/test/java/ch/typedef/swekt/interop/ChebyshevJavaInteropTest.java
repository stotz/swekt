package ch.typedef.swekt.interop;

import ch.typedef.swekt.math.ChebyshevInterpolation;
import kotlin.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Java interoperability tests for Chebyshev interpolation.
 */
public class ChebyshevJavaInteropTest {

    @Test
    public void testEvaluateFromJava() {
        double[] coefficients = {1.0, 2.0, 3.0};
        
        double result = ChebyshevInterpolation.INSTANCE.evaluate(0.5, coefficients);
        
        assertTrue(Double.isFinite(result));
    }

    @Test
    public void testEvaluateDerivativeFromJava() {
        // T_2(x) coefficients: [0, 0, 1]
        double[] coefficients = {0.0, 0.0, 1.0};
        
        // T_2'(0.5) = 4*0.5 = 2.0
        double derivative = ChebyshevInterpolation.INSTANCE.evaluateDerivative(0.5, coefficients);
        
        assertEquals(2.0, derivative, 0.0001);
    }

    @Test
    public void testEvaluateBothFromJava() {
        double[] coefficients = {1.0, 2.0, 3.0};
        
        Pair<Double, Double> result = ChebyshevInterpolation.INSTANCE.evaluateBoth(0.5, coefficients);
        
        double value = result.getFirst();
        double derivative = result.getSecond();
        
        assertTrue(Double.isFinite(value));
        assertTrue(Double.isFinite(derivative));
    }

    @Test
    public void testNormalizeFromJava() {
        double a = 2451545.0;  // J2000.0
        double b = 2451577.0;  // 32 days later
        double jd = 2451561.0; // Middle
        
        double normalized = ChebyshevInterpolation.INSTANCE.normalize(jd, a, b);
        
        assertEquals(0.0, normalized, 0.0001);
    }

    @Test
    public void testDenormalizeFromJava() {
        double a = 100.0;
        double b = 200.0;
        double x = 0.0;
        
        double denormalized = ChebyshevInterpolation.INSTANCE.denormalize(x, a, b);
        
        assertEquals(150.0, denormalized, 0.0001);
    }

    @Test
    public void testRoundTripFromJava() {
        double a = 2451545.0;
        double b = 2451577.0;
        double original = 2451560.0;
        
        double normalized = ChebyshevInterpolation.INSTANCE.normalize(original, a, b);
        double recovered = ChebyshevInterpolation.INSTANCE.denormalize(normalized, a, b);
        
        assertEquals(original, recovered, 0.0001);
    }

    @Test
    public void testEmptyArrayThrowsException() {
        double[] empty = {};
        
        assertThrows(IllegalArgumentException.class, () -> {
            ChebyshevInterpolation.INSTANCE.evaluate(0.0, empty);
        });
    }

    @Test
    public void testConstantPolynomial() {
        double[] coefficients = {5.0};
        
        assertEquals(5.0, ChebyshevInterpolation.INSTANCE.evaluate(-1.0, coefficients), 0.0001);
        assertEquals(5.0, ChebyshevInterpolation.INSTANCE.evaluate(0.0, coefficients), 0.0001);
        assertEquals(5.0, ChebyshevInterpolation.INSTANCE.evaluate(1.0, coefficients), 0.0001);
        
        // Derivative of constant
        assertEquals(0.0, ChebyshevInterpolation.INSTANCE.evaluateDerivative(0.0, coefficients), 0.0001);
    }

    @Test
    public void testLinearPolynomial() {
        // T_1(x) = x
        double[] coefficients = {0.0, 1.0};
        
        assertEquals(-1.0, ChebyshevInterpolation.INSTANCE.evaluate(-1.0, coefficients), 0.0001);
        assertEquals(0.0, ChebyshevInterpolation.INSTANCE.evaluate(0.0, coefficients), 0.0001);
        assertEquals(1.0, ChebyshevInterpolation.INSTANCE.evaluate(1.0, coefficients), 0.0001);
        
        // Derivative is constant 1
        assertEquals(1.0, ChebyshevInterpolation.INSTANCE.evaluateDerivative(0.5, coefficients), 0.0001);
    }

    @Test
    public void testRealWorldCoefficients() {
        // Chebyshev coefficients
        double[] positionCoeffs = {
            1.523,      // T_0 term
            0.00024,    // T_1 term
            -0.00001,   // T_2 term
            0.000001    // T_3 term
        };
        
        double position = ChebyshevInterpolation.INSTANCE.evaluate(0.0, positionCoeffs);
        double velocity = ChebyshevInterpolation.INSTANCE.evaluateDerivative(0.0, positionCoeffs);
        
        // Should be finite and reasonable
        assertTrue(Double.isFinite(position));
        assertTrue(Double.isFinite(velocity));
        assertTrue(Math.abs(position) < 10.0);
        assertTrue(Math.abs(velocity) < 1.0);
    }
}
