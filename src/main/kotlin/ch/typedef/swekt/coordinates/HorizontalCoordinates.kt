package ch.typedef.swekt.coordinates

import kotlin.math.*

/**
 * Horizontal coordinates (altitude-azimuth system).
 *
 * The horizontal coordinate system is observer-specific and depends on:
 * - Observer's geographic location (latitude, longitude)
 * - Time of observation (sidereal time)
 * - Object's equatorial coordinates
 *
 * @property azimuth Azimuth in degrees, 0-360°
 *                   - 0° = North
 *                   - 90° = East
 *                   - 180° = South
 *                   - 270° = West
 * @property altitude Altitude (elevation) in degrees, -90° to +90°
 *                    - 0° = Horizon
 *                    - +90° = Zenith (directly overhead)
 *                    - -90° = Nadir (directly below)
 * @property distance Distance from observer in kilometers
 */
data class HorizontalCoordinates(
    val azimuth: Double,
    val altitude: Double,
    val distance: Double
) {
    init {
        require(azimuth in 0.0..360.0) {
            "Azimuth must be in range [0, 360], got $azimuth"
        }
        require(altitude in -90.0..90.0) {
            "Altitude must be in range [-90, 90], got $altitude"
        }
        require(distance >= 0.0) {
            "Distance must be non-negative, got $distance"
        }
    }

    /**
     * Zenith angle (complement of altitude).
     * 0° = zenith, 90° = horizon, 180° = nadir
     */
    fun zenithAngle(): Double = 90.0 - altitude

    /**
     * Air mass approximation for astronomical observations.
     * Valid for altitudes > 0° (above horizon).
     * 
     * Uses the simple sec(z) approximation for small zenith angles.
     * For z > 60° (alt < 30°), returns approximation.
     */
    fun airMass(): Double {
        if (altitude <= 0.0) return Double.POSITIVE_INFINITY
        
        val z = zenithAngle()
        val zRad = Math.toRadians(z)
        
        return if (z < 60.0) {
            1.0 / cos(zRad)  // Simple sec(z) for small angles
        } else {
            // Hardie (1962) approximation for larger zenith angles
            1.0 / (cos(zRad) + 0.025 * exp(-11.0 * cos(zRad)))
        }
    }

    /**
     * Checks if the object is above the horizon (visible).
     */
    fun isAboveHorizon(): Boolean = altitude > 0.0

    /**
     * Checks if the object is near the zenith (within 30° of directly overhead).
     */
    fun isNearZenith(): Boolean = altitude > 60.0

    /**
     * Returns the cardinal direction (N, NE, E, SE, S, SW, W, NW).
     */
    fun cardinalDirection(): String {
        return when {
            azimuth < 22.5 || azimuth >= 337.5 -> "N"
            azimuth < 67.5 -> "NE"
            azimuth < 112.5 -> "E"
            azimuth < 157.5 -> "SE"
            azimuth < 202.5 -> "S"
            azimuth < 247.5 -> "SW"
            azimuth < 292.5 -> "W"
            else -> "NW"
        }
    }

    /**
     * Formats coordinates for display.
     * Example: "Az: 45.5°, Alt: 30.2° (NE, above horizon)"
     */
    fun toDisplayString(): String {
        val visibility = if (isAboveHorizon()) "above horizon" else "below horizon"
        return "Az: ${"%.1f".format(azimuth)}°, Alt: ${"%.1f".format(altitude)}° (${cardinalDirection()}, $visibility)"
    }

    companion object {
        /**
         * Creates horizontal coordinates with azimuth normalization.
         */
        @JvmStatic
        fun of(azimuth: Double, altitude: Double, distance: Double): HorizontalCoordinates {
            val normalizedAz = ((azimuth % 360.0) + 360.0) % 360.0
            return HorizontalCoordinates(normalizedAz, altitude, distance)
        }
    }
}
