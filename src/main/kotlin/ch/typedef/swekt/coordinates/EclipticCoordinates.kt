package ch.typedef.swekt.coordinates

import kotlin.math.*

/**
 * Ecliptic coordinates (heliocentric or geocentric).
 *
 * The ecliptic coordinate system uses the plane of Earth's orbit around the Sun as reference.
 * This is the primary coordinate system for astrology.
 *
 * @property longitude Ecliptic longitude λ (lambda) in degrees, 0-360°
 *                     - 0° = Vernal Equinox (♈ Aries)
 *                     - 90° = Summer Solstice (♋ Cancer)
 *                     - 180° = Autumnal Equinox (♎ Libra)
 *                     - 270° = Winter Solstice (♑ Capricorn)
 * @property latitude Ecliptic latitude β (beta) in degrees, -90° to +90°
 *                    - 0° = on the ecliptic plane
 *                    - positive = north of ecliptic
 *                    - negative = south of ecliptic
 * @property distance Distance from origin in kilometers
 */
data class EclipticCoordinates(
    val longitude: Double,
    val latitude: Double,
    val distance: Double
) {
    init {
        require(longitude in 0.0..360.0) {
            "Longitude must be in range [0, 360], got $longitude"
        }
        require(latitude in -90.0..90.0) {
            "Latitude must be in range [-90, 90], got $latitude"
        }
        require(distance >= 0.0) {
            "Distance must be non-negative, got $distance"
        }
    }

    /**
     * Returns the zodiac sign for this longitude.
     * Each sign covers 30 degrees starting from Aries at 0°.
     */
    fun zodiacSign(): ZodiacSign {
        val signIndex = (longitude / 30.0).toInt()
        return ZodiacSign.entries[signIndex.coerceIn(0, 11)]
    }

    /**
     * Returns the position within the current zodiac sign (0-30°).
     */
    fun positionInSign(): Double = longitude % 30.0

    /**
     * Formats coordinates in traditional astrological notation.
     * Example: "15°30' ♈ Aries"
     */
    fun toAstrologicalString(): String {
        val sign = zodiacSign()
        val posInSign = positionInSign()
        val degrees = posInSign.toInt()
        val minutes = ((posInSign - degrees) * 60).toInt()
        
        // Format sign name: "ARIES" -> "Aries"
        val signName = sign.name.lowercase().replaceFirstChar { it.uppercase() }
        
        return "${degrees}°${minutes.toString().padStart(2, '0')}' ${sign.symbol} $signName"
    }

    companion object {
        /**
         * Creates ecliptic coordinates with longitude normalization.
         */
        @JvmStatic
        fun of(longitude: Double, latitude: Double, distance: Double): EclipticCoordinates {
            val normalizedLon = ((longitude % 360.0) + 360.0) % 360.0
            return EclipticCoordinates(normalizedLon, latitude, distance)
        }
    }
}

/**
 * Zodiac signs in tropical astrology.
 * Each sign covers 30 degrees of ecliptic longitude.
 */
enum class ZodiacSign(
    val symbol: String,
    val element: Element,
    val quality: Quality
) {
    ARIES("♈", Element.FIRE, Quality.CARDINAL),
    TAURUS("♉", Element.EARTH, Quality.FIXED),
    GEMINI("♊", Element.AIR, Quality.MUTABLE),
    CANCER("♋", Element.WATER, Quality.CARDINAL),
    LEO("♌", Element.FIRE, Quality.FIXED),
    VIRGO("♍", Element.EARTH, Quality.MUTABLE),
    LIBRA("♎", Element.AIR, Quality.CARDINAL),
    SCORPIO("♏", Element.WATER, Quality.FIXED),
    SAGITTARIUS("♐", Element.FIRE, Quality.MUTABLE),
    CAPRICORN("♑", Element.EARTH, Quality.CARDINAL),
    AQUARIUS("♒", Element.AIR, Quality.FIXED),
    PISCES("♓", Element.WATER, Quality.MUTABLE);

    /**
     * Start longitude of this sign (0°, 30°, 60°, etc.)
     */
    fun startLongitude(): Double = ordinal * 30.0

    /**
     * End longitude of this sign (30°, 60°, 90°, etc.)
     */
    fun endLongitude(): Double = (ordinal + 1) * 30.0

    enum class Element {
        FIRE, EARTH, AIR, WATER
    }

    enum class Quality {
        CARDINAL, FIXED, MUTABLE
    }
}
