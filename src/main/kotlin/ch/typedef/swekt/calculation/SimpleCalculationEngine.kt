package ch.typedef.swekt.calculation

import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet
import kotlin.math.*

/**
 * Simple calculation engine using analytical approximations.
 *
 * This engine uses simplified algorithms similar to those in AA+ or Moshier's
 * ephemeris code. These provide good accuracy for most applications:
 * - Sun: ~0.01 degrees accuracy
 * - Moon: ~0.5 degrees accuracy
 *
 * For higher precision, use the full Swiss Ephemeris engine with .se1 files.
 *
 * Currently supports:
 * - Sun (using simplified VSOP87)
 * - Moon (using ELP2000-82B approximation)
 */
class SimpleCalculationEngine {

    /**
     * Calculate planetary position at given time.
     *
     * @param planet The celestial body to calculate
     * @param julianDay The time
     * @return Calculated position
     * @throws UnsupportedOperationException if planet not yet supported
     */
    fun calculate(planet: Planet, julianDay: JulianDay): PlanetaryPosition {
        return when (planet) {
            Planet.SUN -> calculateSun(julianDay)
            Planet.MOON -> calculateMoon(julianDay)
            else -> throw UnsupportedOperationException(
                "Planet ${planet.displayName} not yet supported. " +
                        "Currently only Sun and Moon are implemented."
            )
        }
    }

    /**
     * Calculates Sun position using simplified VSOP87 algorithm.
     *
     * Based on Jean Meeus "Astronomical Algorithms" (1998), Chapter 25.
     * Accuracy: ~0.01 degrees (good for most applications)
     *
     * @param julianDay Time
     * @return Sun position
     */
    private fun calculateSun(julianDay: JulianDay): PlanetaryPosition {
        // Julian centuries from J2000.0
        val T = (julianDay.value - 2451545.0) / 36525.0

        // Mean longitude of Sun (degrees)
        val L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T * T

        // Mean anomaly of Sun (degrees)
        val M = 357.52911 + 35999.05029 * T - 0.0001537 * T * T

        // Convert to radians
        val Mrad = Math.toRadians(M)

        // Equation of center (main correction to mean longitude)
        val C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * sin(Mrad) +
                (0.019993 - 0.000101 * T) * sin(2 * Mrad) +
                0.000289 * sin(3 * Mrad)

        // True longitude
        var longitude = L0 + C

        // Normalize to 0-360
        longitude = normalizeAngle(longitude)

        // True anomaly
        val v = M + C

        // Eccentricity of Earth's orbit
        val e = 0.016708634 - 0.000042037 * T - 0.0000001267 * T * T

        // Distance (AU) using Kepler's equation
        val distance = (1.000001018 * (1 - e * e)) / (1 + e * cos(Math.toRadians(v)))

        // Sun always at ecliptic latitude 0 (by definition of ecliptic)
        val latitude = 0.0
        val latitudeSpeed = 0.0

        // Mean motion (degrees/day) - this is approximate
        val longitudeSpeed = 0.9856474 // Mean daily motion of Sun

        // Distance speed (AU/day) - very small, simplified
        // For more accuracy: compute from orbital mechanics
        val distanceSpeed = 0.0

        return PlanetaryPosition(
            planet = Planet.SUN,
            julianDay = julianDay,
            longitude = longitude,
            latitude = latitude,
            distance = distance,
            longitudeSpeed = longitudeSpeed,
            latitudeSpeed = latitudeSpeed,
            distanceSpeed = distanceSpeed
        )
    }

    /**
     * Calculates Moon position using simplified ELP2000-82B algorithm.
     *
     * Based on Jean Meeus "Astronomical Algorithms" (1998), Chapter 47.
     * Accuracy: ~10 arcminutes (0.17 degrees) - sufficient for most uses
     *
     * @param julianDay Time
     * @return Moon position
     */
    private fun calculateMoon(julianDay: JulianDay): PlanetaryPosition {
        // Julian centuries from J2000.0
        val T = (julianDay.value - 2451545.0) / 36525.0

        // Moon's mean longitude (degrees)
        val Lprime = 218.3164477 + 481267.88123421 * T -
                0.0015786 * T * T + T * T * T / 538841.0 - T * T * T * T / 65194000.0

        // Mean elongation of Moon (degrees)
        val D = 297.8501921 + 445267.1114034 * T -
                0.0018819 * T * T + T * T * T / 545868.0 - T * T * T * T / 113065000.0

        // Sun's mean anomaly (degrees)
        val M = 357.5291092 + 35999.0502909 * T -
                0.0001536 * T * T + T * T * T / 24490000.0

        // Moon's mean anomaly (degrees)
        val Mprime = 134.9633964 + 477198.8675055 * T +
                0.0087414 * T * T + T * T * T / 69699.0 - T * T * T * T / 14712000.0

        // Moon's argument of latitude (degrees)
        val F = 93.2720950 + 483202.0175233 * T -
                0.0036539 * T * T - T * T * T / 3526000.0 + T * T * T * T / 863310000.0

        // Convert to radians for trig functions
        val Drad = Math.toRadians(D)
        val Mrad = Math.toRadians(M)
        val Mprimerad = Math.toRadians(Mprime)
        val Frad = Math.toRadians(F)

        // Main periodic terms for longitude (simplified - using most significant terms)
        val longitudeCorrection = 6.288774 * sin(Mprimerad) +
                1.274027 * sin(2 * Drad - Mprimerad) +
                0.658314 * sin(2 * Drad) +
                0.213618 * sin(2 * Mprimerad) -
                0.185116 * sin(Mrad) -
                0.114332 * sin(2 * Frad) +
                0.058793 * sin(2 * Drad - 2 * Mprimerad) +
                0.057066 * sin(2 * Drad - Mrad - Mprimerad) +
                0.053322 * sin(2 * Drad + Mprimerad) +
                0.045758 * sin(2 * Drad - Mrad)

        // True longitude
        var longitude = Lprime + longitudeCorrection
        longitude = normalizeAngle(longitude)

        // Main periodic terms for latitude
        val latitude = 5.128122 * sin(Frad) +
                0.280602 * sin(Mprimerad + Frad) +
                0.277693 * sin(Mprimerad - Frad) +
                0.173237 * sin(2 * Drad - Frad) +
                0.055413 * sin(2 * Drad - Mprimerad + Frad) +
                0.046271 * sin(2 * Drad - Mprimerad - Frad) +
                0.032573 * sin(2 * Drad + Frad)

        // Distance (Earth-Moon in AU)
        // 385000 km mean distance = 0.00257 AU
        val distanceCorrection = -20905.355 * cos(Mprimerad) -
                3699.111 * cos(2 * Drad - Mprimerad) -
                2955.968 * cos(2 * Drad) -
                569.925 * cos(2 * Mprimerad) +
                246.158 * cos(2 * Mrad) -
                204.586 * cos(2 * Mrad - Mprimerad) -
                170.733 * cos(2 * Drad + Mprimerad) -
                152.138 * cos(2 * Drad - Mrad - Mprimerad)

        val distanceKm = 385000.56 + distanceCorrection
        val distance = distanceKm / 149597870.7 // Convert km to AU

        // Moon's mean motion: ~13.176° per day
        val longitudeSpeed = 13.176358

        // Latitude speed (approximate)
        val latitudeSpeed = 0.0 // Simplified - actual varies

        // Distance speed (approximate)
        val distanceSpeed = 0.0 // Simplified - actual varies

        return PlanetaryPosition(
            planet = Planet.MOON,
            julianDay = julianDay,
            longitude = longitude,
            latitude = latitude,
            distance = distance,
            longitudeSpeed = longitudeSpeed,
            latitudeSpeed = latitudeSpeed,
            distanceSpeed = distanceSpeed
        )
    }

    /**
     * Normalize angle to 0-360 degrees range.
     */
    private fun normalizeAngle(angle: Double): Double {
        var result = angle % 360.0
        if (result < 0.0) result += 360.0
        return result
    }
}
