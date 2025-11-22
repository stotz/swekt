package ch.typedef.swekt.coordinates

import kotlin.math.*

/**
 * Equatorial coordinates (celestial coordinates).
 *
 * The equatorial coordinate system uses Earth's equator projected onto the celestial sphere.
 * This is the primary coordinate system for astronomy and star catalogs.
 *
 * @property rightAscension Right ascension α (alpha) in hours, 0-24h
 *                          - 0h = Vernal Equinox
 *                          - 6h = 90° east
 *                          - 12h = 180° (Autumnal Equinox)
 *                          - 18h = 270° west
 * @property declination Declination δ (delta) in degrees, -90° to +90°
 *                       - 0° = celestial equator
 *                       - +90° = north celestial pole
 *                       - -90° = south celestial pole
 * @property distance Distance from origin in kilometers
 */
data class EquatorialCoordinates(
    val rightAscension: Double,  // in hours (0-24)
    val declination: Double,     // in degrees (-90 to +90)
    val distance: Double         // in kilometers
) {
    init {
        require(rightAscension in 0.0..24.0) {
            "Right ascension must be in range [0, 24] hours, got $rightAscension"
        }
        require(declination in -90.0..90.0) {
            "Declination must be in range [-90, 90] degrees, got $declination"
        }
        require(distance >= 0.0) {
            "Distance must be non-negative, got $distance"
        }
    }

    /**
     * Right ascension in degrees (0-360°).
     */
    fun rightAscensionDegrees(): Double = rightAscension * 15.0  // 1 hour = 15 degrees

    /**
     * Formats right ascension in HMS (hours, minutes, seconds) notation.
     * Example: "12h 34m 56s"
     */
    fun rightAscensionHMS(): String {
        val hours = rightAscension.toInt()
        val minutes = ((rightAscension - hours) * 60).toInt()
        val seconds = ((rightAscension - hours - minutes / 60.0) * 3600).toInt()
        
        return "${hours}h ${minutes.toString().padStart(2, '0')}m ${seconds.toString().padStart(2, '0')}s"
    }

    /**
     * Formats declination in DMS (degrees, arcminutes, arcseconds) notation.
     * Example: "+45° 30' 15"" or "-12° 05' 30""
     */
    fun declinationDMS(): String {
        val sign = if (declination >= 0) "+" else "-"
        val absDec = abs(declination)
        val degrees = absDec.toInt()
        val minutes = ((absDec - degrees) * 60).toInt()
        val seconds = ((absDec - degrees - minutes / 60.0) * 3600).toInt()
        
        return "$sign${degrees}° ${minutes.toString().padStart(2, '0')}' ${seconds.toString().padStart(2, '0')}\""
    }

    /**
     * Formats coordinates in astronomical notation.
     * Example: "RA: 12h 34m 56s, Dec: +45° 30' 15""
     */
    fun toAstronomicalString(): String {
        return "RA: ${rightAscensionHMS()}, Dec: ${declinationDMS()}"
    }

    companion object {
        /**
         * Creates equatorial coordinates from right ascension in degrees.
         */
        @JvmStatic
        fun fromDegrees(raDegrees: Double, decDegrees: Double, distance: Double): EquatorialCoordinates {
            val raHours = ((raDegrees % 360.0 + 360.0) % 360.0) / 15.0
            return EquatorialCoordinates(raHours, decDegrees, distance)
        }

        /**
         * Creates equatorial coordinates with right ascension normalization.
         */
        @JvmStatic
        fun of(raHours: Double, decDegrees: Double, distance: Double): EquatorialCoordinates {
            val normalizedRA = ((raHours % 24.0) + 24.0) % 24.0
            return EquatorialCoordinates(normalizedRA, decDegrees, distance)
        }
    }
}
