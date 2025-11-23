package ch.typedef.swekt.houses

import ch.typedef.swekt.model.JulianDay

/**
 * House system identifier.
 *
 * Each astrological house system divides the ecliptic into 12 houses
 * using different mathematical approaches. The choice of house system
 * affects the interpretation of a birth chart.
 */
enum class HouseSystem(val code: Char, val displayName: String) {
    /**
     * Placidus - Most popular modern system.
     * Based on trisecting the diurnal and nocturnal semi-arcs.
     * Does not work well in polar regions.
     */
    PLACIDUS('P', "Placidus"),

    /**
     * Koch - Popular modern alternative to Placidus.
     * Based on birth place (topocentric).
     * Does not work well in polar regions.
     */
    KOCH('K', "Koch"),

    /**
     * Porphyry - Simple system dividing each quadrant into 3 equal parts.
     * Works in all geographic locations.
     */
    PORPHYRY('O', "Porphyry"),

    /**
     * Regiomontanus - Medieval system.
     * Divides the celestial equator into 12 equal parts.
     */
    REGIOMONTANUS('R', "Regiomontanus"),

    /**
     * Campanus - Medieval system.
     * Divides the prime vertical into 12 equal parts.
     */
    CAMPANUS('C', "Campanus"),

    /**
     * Equal - Simplest system.
     * Divides ecliptic into 12 equal 30° sections starting from Ascendant.
     */
    EQUAL('A', "Equal"),

    /**
     * Equal from MC - Equal houses starting from MC instead of Ascendant.
     */
    EQUAL_MC('D', "Equal (MC)"),

    /**
     * Whole Sign - Ancient system.
     * Each zodiac sign = one complete house.
     * Very simple, used in Hellenistic astrology.
     */
    WHOLE_SIGN('W', "Whole Sign"),

    /**
     * Vehlow Equal - Equal houses with different cusp definition.
     * Cusps are in the middle of houses, not at the beginning.
     */
    VEHLOW('V', "Vehlow Equal"),

    /**
     * Alcabitius - Arabic system.
     * Based on the meridian and horizon.
     */
    ALCABITIUS('B', "Alcabitius"),

    /**
     * Azimuthal / Horizontal - Based on azimuth.
     * Similar to Campanus but uses horizontal coordinates.
     */
    AZIMUTHAL('H', "Horizontal/Azimuthal"),

    /**
     * Polich/Page (Topocentric) - Modern system.
     * Uses topocentric coordinates.
     */
    TOPOCENTRIC('T', "Topocentric (Polich/Page)"),

    /**
     * Morinus - Equatorial system.
     * Divides the equator into 12 equal parts.
     */
    MORINUS('M', "Morinus"),

    /**
     * Meridian / Axial Rotation - Based on Earth's rotation axis.
     */
    MERIDIAN('X', "Axial Rotation/Meridian"),

    /**
     * Gauquelin Sectors - 36 sectors instead of 12 houses.
     * Used in statistical astrology research.
     */
    GAUQUELIN('G', "Gauquelin Sectors");

    companion object {
        /**
         * Get house system by code character.
         */
        @JvmStatic
        fun fromCode(code: Char): HouseSystem? {
            return entries.find { it.code == code || it.code == code.uppercaseChar() }
        }

        /**
         * Get house system by name (case-insensitive).
         */
        @JvmStatic
        fun fromName(name: String): HouseSystem? {
            return entries.find { 
                it.displayName.equals(name, ignoreCase = true) || 
                it.name.equals(name, ignoreCase = true) 
            }
        }
    }
}

/**
 * Result of house calculation containing cusps and angles.
 *
 * @property cusps Array of 12 (or 36 for Gauquelin) house cusps in degrees (0-360).
 *                 cusp[0] is unused, cusp[1] = house 1 (usually Ascendant),
 *                 cusp[10] = house 10 (usually MC).
 * @property ascendant Ascendant in degrees (0-360)
 * @property mc Medium Coeli (Midheaven) in degrees (0-360)
 * @property armc Right Ascension of MC in degrees (0-360)
 * @property vertex Vertex point in degrees (0-360)
 * @property equatorialAscendant Equatorial Ascendant in degrees (0-360)
 * @property coAscendantKoch Co-Ascendant (Koch) in degrees (0-360)
 * @property coAscendantMunkasey Co-Ascendant (Munkasey) in degrees (0-360)
 * @property polarAscendant Polar Ascendant (Munkasey) in degrees (0-360)
 */
data class HouseCusps(
    val cusps: DoubleArray,
    val ascendant: Double,
    val mc: Double,
    val armc: Double,
    val vertex: Double,
    val equatorialAscendant: Double,
    val coAscendantKoch: Double,
    val coAscendantMunkasey: Double,
    val polarAscendant: Double
) {
    /**
     * Get a specific house cusp (1-12 or 1-36 for Gauquelin).
     */
    fun getCusp(house: Int): Double {
        require(house in 1 until cusps.size) { 
            "House must be between 1 and ${cusps.size - 1}, got: $house" 
        }
        return cusps[house]
    }

    /**
     * Get the Descendant (opposite of Ascendant).
     */
    val descendant: Double
        get() = (ascendant + 180.0) % 360.0

    /**
     * Get the Imum Coeli (IC, opposite of MC).
     */
    val ic: Double
        get() = (mc + 180.0) % 360.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HouseCusps

        if (!cusps.contentEquals(other.cusps)) return false
        if (ascendant != other.ascendant) return false
        if (mc != other.mc) return false
        if (armc != other.armc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cusps.contentHashCode()
        result = 31 * result + ascendant.hashCode()
        result = 31 * result + mc.hashCode()
        result = 31 * result + armc.hashCode()
        return result
    }
}

/**
 * Geographic location for house calculations.
 *
 * @property latitude Geographic latitude in degrees (-90 to +90).
 *                    Positive for North, negative for South.
 * @property longitude Geographic longitude in degrees (-180 to +180).
 *                     Positive for East, negative for West.
 * @property elevation Elevation above sea level in meters (optional).
 */
data class GeographicLocation(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0
) {
    init {
        require(latitude in -90.0..90.0) { 
            "Latitude must be between -90 and +90 degrees, got: $latitude" 
        }
        require(longitude in -180.0..180.0) { 
            "Longitude must be between -180 and +180 degrees, got: $longitude" 
        }
        require(elevation >= -500.0) { 
            "Elevation must be >= -500m (Dead Sea level), got: $elevation" 
        }
    }

    /**
     * Format location as string (e.g., "47.5°N, 7.6°E").
     */
    override fun toString(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return "${kotlin.math.abs(latitude)}°$latDir, ${kotlin.math.abs(longitude)}°$lonDir"
    }

    companion object {
        /**
         * Create location from degrees, minutes, seconds.
         */
        @JvmStatic
        fun fromDMS(
            latDegrees: Int,
            latMinutes: Int,
            latSeconds: Double,
            latNorth: Boolean,
            lonDegrees: Int,
            lonMinutes: Int,
            lonSeconds: Double,
            lonEast: Boolean,
            elevation: Double = 0.0
        ): GeographicLocation {
            val lat = (latDegrees + latMinutes / 60.0 + latSeconds / 3600.0) * 
                      if (latNorth) 1.0 else -1.0
            val lon = (lonDegrees + lonMinutes / 60.0 + lonSeconds / 3600.0) * 
                      if (lonEast) 1.0 else -1.0
            return GeographicLocation(lat, lon, elevation)
        }

        /**
         * Famous locations for testing.
         */
        @JvmField
        val GREENWICH = GeographicLocation(51.4769, 0.0)
        
        @JvmField
        val NEW_YORK = GeographicLocation(40.7128, -74.0060)
        
        @JvmField
        val TOKYO = GeographicLocation(35.6762, 139.6503)
        
        @JvmField
        val SYDNEY = GeographicLocation(-33.8688, 151.2093)
    }
}
