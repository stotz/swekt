package ch.typedef.swekt.calculation

import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Represents the calculated position of a celestial body at a specific time.
 *
 * All coordinates are heliocentric ecliptic coordinates:
 * - Longitude: 0-360 degrees (ecliptic longitude)
 * - Latitude: -90 to +90 degrees (ecliptic latitude)
 * - Distance: astronomical units (AU)
 *
 * Speed values represent the daily motion in respective units.
 *
 * @property planet The celestial body
 * @property julianDay Time of calculation
 * @property longitude Ecliptic longitude in degrees (0-360)
 * @property latitude Ecliptic latitude in degrees (-90 to +90)
 * @property distance Distance from Earth in AU (positive)
 * @property longitudeSpeed Daily motion in longitude (degrees/day)
 * @property latitudeSpeed Daily motion in latitude (degrees/day)
 * @property distanceSpeed Daily change in distance (AU/day)
 */
data class PlanetaryPosition(
    val planet: Planet,
    val julianDay: JulianDay,
    val longitude: Double,
    val latitude: Double,
    val distance: Double,
    val longitudeSpeed: Double,
    val latitudeSpeed: Double,
    val distanceSpeed: Double
) {
    init {
        require(longitude in 0.0..360.0) {
            "Longitude must be between 0 and 360 degrees, got $longitude"
        }
        require(latitude in -90.0..90.0) {
            "Latitude must be between -90 and +90 degrees, got $latitude"
        }
        require(distance > 0.0) {
            "Distance must be positive, got $distance"
        }
    }

    override fun toString(): String {
        return "PlanetaryPosition(" +
                "planet=${planet.name}, " +
                "jd=${julianDay.value}, " +
                "lon=${String.format(java.util.Locale.US, "%.2f", longitude)}°, " +
                "lat=${String.format(java.util.Locale.US, "%.2f", latitude)}°, " +
                "dist=${String.format(java.util.Locale.US, "%.4f", distance)} AU)"
    }
}
