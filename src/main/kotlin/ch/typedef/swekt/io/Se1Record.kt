package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay

/**
 * A single record from a Swiss Ephemeris SE1 file.
 *
 * SE1 files store planetary positions as Chebyshev polynomial coefficients.
 * Each record covers a specific time span (typically 32 days) and contains
 * coefficients for three coordinates:
 * - Longitude (ecliptic longitude in degrees)
 * - Latitude (ecliptic latitude in degrees)
 * - Distance (distance from Sun in AU)
 *
 * The coefficients are used with Chebyshev interpolation to calculate
 * positions at any time within the record's time span.
 *
 * @property startJulianDay Start of time range (inclusive)
 * @property endJulianDay End of time range (inclusive)
 * @property longitudeCoefficients Chebyshev coefficients for longitude
 * @property latitudeCoefficients Chebyshev coefficients for latitude
 * @property distanceCoefficients Chebyshev coefficients for distance
 */
data class Se1Record(
    val startJulianDay: JulianDay,
    val endJulianDay: JulianDay,
    val longitudeCoefficients: DoubleArray,
    val latitudeCoefficients: DoubleArray,
    val distanceCoefficients: DoubleArray
) {
    /**
     * Time span covered by this record in days.
     */
    val timeSpan: Double
        get() = endJulianDay.value - startJulianDay.value

    /**
     * Midpoint of the time range.
     */
    val midpoint: JulianDay
        get() = JulianDay((startJulianDay.value + endJulianDay.value) / 2.0)

    /**
     * Checks if a given Julian Day falls within this record's time range.
     *
     * @param julianDay Time to check
     * @return True if julianDay is in [startJulianDay, endJulianDay]
     */
    fun contains(julianDay: JulianDay): Boolean {
        return julianDay.value >= startJulianDay.value && 
               julianDay.value <= endJulianDay.value
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Se1Record

        if (startJulianDay != other.startJulianDay) return false
        if (endJulianDay != other.endJulianDay) return false
        if (!longitudeCoefficients.contentEquals(other.longitudeCoefficients)) return false
        if (!latitudeCoefficients.contentEquals(other.latitudeCoefficients)) return false
        if (!distanceCoefficients.contentEquals(other.distanceCoefficients)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = startJulianDay.hashCode()
        result = 31 * result + endJulianDay.hashCode()
        result = 31 * result + longitudeCoefficients.contentHashCode()
        result = 31 * result + latitudeCoefficients.contentHashCode()
        result = 31 * result + distanceCoefficients.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Se1Record(" +
                "timeRange=[${startJulianDay.value}, ${endJulianDay.value}], " +
                "span=${String.format("%.1f", timeSpan)} days, " +
                "coeffs=[${longitudeCoefficients.size}, ${latitudeCoefficients.size}, ${distanceCoefficients.size}])"
    }
}
