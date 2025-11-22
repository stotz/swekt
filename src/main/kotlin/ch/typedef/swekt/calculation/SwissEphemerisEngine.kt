package ch.typedef.swekt.calculation

import ch.typedef.swekt.io.Se1Record
import ch.typedef.swekt.math.ChebyshevInterpolation
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Swiss Ephemeris calculation engine.
 *
 * Uses Chebyshev polynomial interpolation on SE1 file data to calculate
 * high-precision planetary positions.
 *
 * This engine integrates:
 * - SE1 binary file format
 * - Chebyshev interpolation (ACM Algorithm 446)
 * - Coordinate transformations
 *
 * For very high precision calculations (sub-arcsecond accuracy).
 */
class SwissEphemerisEngine {

    /**
     * Calculates planetary position from an SE1 record.
     *
     * Takes Chebyshev coefficients from the record and interpolates
     * position and velocity at the given Julian Day.
     *
     * @param planet The planet (for result metadata)
     * @param julianDay Time at which to calculate
     * @param record SE1 record containing Chebyshev coefficients
     * @return Planetary position with velocities
     * @throws IllegalArgumentException if julianDay is outside record's time range
     */
    fun calculateFromRecord(
        planet: Planet,
        julianDay: JulianDay,
        record: Se1Record
    ): PlanetaryPosition {
        // Validate that Julian Day is within record's time range
        require(record.contains(julianDay)) {
            "Julian Day ${julianDay.value} is outside record time range " +
                    "[${record.startJulianDay.value}, ${record.endJulianDay.value}]"
        }

        // Normalize Julian Day to Chebyshev domain [-1, 1]
        val normalizedTime = ChebyshevInterpolation.normalize(
            julianDay.value,
            record.startJulianDay.value,
            record.endJulianDay.value
        )

        // Calculate longitude and its derivative (speed)
        val (longitude, lonSpeedNormalized) = if (record.longitudeCoefficients.isNotEmpty()) {
            ChebyshevInterpolation.evaluateBoth(normalizedTime, record.longitudeCoefficients)
        } else {
            Pair(0.0, 0.0)
        }

        // Calculate latitude and its derivative
        val (latitude, latSpeedNormalized) = if (record.latitudeCoefficients.isNotEmpty()) {
            ChebyshevInterpolation.evaluateBoth(normalizedTime, record.latitudeCoefficients)
        } else {
            Pair(0.0, 0.0)
        }

        // Calculate distance and its derivative
        val (distance, distSpeedNormalized) = if (record.distanceCoefficients.isNotEmpty()) {
            ChebyshevInterpolation.evaluateBoth(normalizedTime, record.distanceCoefficients)
        } else {
            Pair(1.0, 0.0)
        }

        // Convert speeds from "per normalized time" to "per Julian Day"
        // 
        // Normalized time x ∈ [-1, 1] maps to [startJd, endJd]
        // So dx/dt_normalized = dx/dt_julian * dt_julian/dt_normalized
        // 
        // Since dt_normalized/dt_julian = 2 / (endJd - startJd):
        // dx/dt_julian = dx/dt_normalized * (endJd - startJd) / 2
        val timeScale = record.timeSpan / 2.0
        
        val longitudeSpeed = lonSpeedNormalized / timeScale
        val latitudeSpeed = latSpeedNormalized / timeScale
        val distanceSpeed = distSpeedNormalized / timeScale

        return PlanetaryPosition(
            planet = planet,
            julianDay = julianDay,
            longitude = normalizeAngle(longitude),
            latitude = latitude,
            distance = distance,
            longitudeSpeed = longitudeSpeed,
            latitudeSpeed = latitudeSpeed,
            distanceSpeed = distanceSpeed
        )
    }

    /**
     * Normalizes an angle to [0, 360) degrees.
     */
    private fun normalizeAngle(angle: Double): Double {
        var result = angle % 360.0
        if (result < 0.0) result += 360.0
        return result
    }
}
