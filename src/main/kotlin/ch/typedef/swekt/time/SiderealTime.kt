package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay
import kotlin.math.*

/**
 * Sidereal Time calculations for astronomical observations.
 *
 * Sidereal time is the hour angle of the vernal equinox, representing
 * the Earth's rotation relative to the fixed stars rather than the Sun.
 *
 * Types of Sidereal Time:
 * - GMST (Greenwich Mean Sidereal Time): Mean sidereal time at Greenwich
 * - GAST (Greenwich Apparent Sidereal Time): GMST corrected for nutation
 * - LST (Local Sidereal Time): GMST or GAST at a specific longitude
 *
 * Key Relationships:
 * - Sidereal day ≈ 23h 56m 4.1s (solar time)
 * - Solar day ≈ 24h 3m 56s (sidereal time)
 * - 1 sidereal hour = 15° of longitude
 * - LST = GMST + longitude (in hours)
 *
 * Critical for:
 * - House calculations (requires LST)
 * - Rise/set/transit calculations
 * - Converting between equatorial and horizontal coordinates
 * - Telescope pointing
 *
 * Implementation based on:
 * - IAU 2006 Resolution B1 (high precision)
 * - N. Capitaine, P.T. Wallace, J. Chapront, A&A 412, 567-586 (2003)
 * - Swiss Ephemeris swephlib.c
 * - Meeus, "Astronomical Algorithms" (1998), Chapter 12
 *
 * Accuracy:
 * IAU 2006 provides sub-microarcsecond accuracy (< 0.001 seconds)
 * for precise astronomical and astrological calculations.
 */
object SiderealTime {

    /**
     * J2000.0 epoch
     */
    private const val J2000 = 2451545.0

    /**
     * Seconds per day
     */
    private const val SECONDS_PER_DAY = 86400.0

    /**
     * Calculates Greenwich Mean Sidereal Time (GMST) for a given Julian Day.
     *
     * GMST is the hour angle of the mean vernal equinox at Greenwich.
     * This uses the IAU 2006 formula for high precision.
     *
     * Formula from:
     * - IAU 2006 Resolution B1
     * - N. Capitaine, P.T. Wallace, J. Chapront, "Expressions for IAU 2000 precession quantities", A&A 412, 567-586 (2003)
     * - Swiss Ephemeris swephlib.c line 64-69
     *
     * @param julianDay Julian Day (UT)
     * @return GMST in hours (0-24)
     */
    @JvmStatic
    fun calculateGMST(julianDay: JulianDay): Double {
        val jd = julianDay.value
        
        // Split into midnight and time of day (Swiss Ephemeris method)
        var jd0 = floor(jd)
        var secs = jd - jd0
        
        if (secs < 0.5) {
            jd0 -= 0.5
            secs += 0.5
        } else {
            jd0 += 0.5
            secs -= 0.5
        }
        secs *= 86400.0  // Convert to seconds
        
        // UT1 centuries from J2000
        val tu = (jd0 - J2000) / 36525.0
        
        // TT centuries from J2000 (requires Delta T)
        val deltaT = DeltaT.calculate(JulianDay(jd0))  // in days
        val tt = (jd0 + deltaT - J2000) / 36525.0
        
        // IAU 2006 formula: GMST at 0h UT in seconds
        // Complex polynomial with UT/TT cross-term
        var gmst = (((-0.000000002454 * tt - 0.00000199708) * tt - 0.0000002926) * tt + 0.092772110) * tt * tt +
                   307.4771013 * (tt - tu) +
                   8640184.79447825 * tu +
                   24110.5493771
        
        // Mean solar days per sidereal day at date
        // Derivative of GMST (can assume UT1 ≈ TT)
        val msday = 1.0 + ((((-0.000000012270 * tt - 0.00000798832) * tt - 0.0000008778) * tt + 0.185544220) * tt +
                   8640184.79447825) / (86400.0 * 36525.0)
        
        // Add time since midnight
        gmst += msday * secs
        
        // Normalize to 0-86400 seconds (one sidereal day)
        gmst = gmst - 86400.0 * floor(gmst / 86400.0)
        
        // Convert to hours and return
        return gmst / 3600.0
    }

    /**
     * Calculates GMST at 0h UT (midnight) for a given date.
     *
     * Formula from IAU 2006.
     *
     * @param julianDay Julian Day (UT)
     * @return GMST at 0h UT in hours (0-24)
     */
    @JvmStatic
    fun calculateGMST0(julianDay: JulianDay): Double {
        val jd = julianDay.value
        
        // Get midnight JD (Swiss Ephemeris method)
        var jd0 = floor(jd)
        var secs = jd - jd0
        
        if (secs < 0.5) {
            jd0 -= 0.5
        } else {
            jd0 += 0.5
        }
        
        // UT1 centuries from J2000
        val tu = (jd0 - J2000) / 36525.0
        
        // TT centuries from J2000
        val deltaT = DeltaT.calculate(JulianDay(jd0))
        val tt = (jd0 + deltaT - J2000) / 36525.0
        
        // IAU 2006 formula - GMST at 0h UT in seconds
        var gmst = (((-0.000000002454 * tt - 0.00000199708) * tt - 0.0000002926) * tt + 0.092772110) * tt * tt +
                   307.4771013 * (tt - tu) +
                   8640184.79447825 * tu +
                   24110.5493771
        
        // Normalize to 0-86400 seconds
        gmst = gmst - 86400.0 * floor(gmst / 86400.0)
        
        // Convert to hours
        return gmst / 3600.0
    }

    /**
     * Calculates Greenwich Apparent Sidereal Time (GAST).
     *
     * GAST = GMST + equation of equinoxes
     *
     * The equation of equinoxes is the difference between apparent
     * and mean sidereal time, caused by nutation of the Earth's axis.
     *
     * @param julianDay Julian Day (UT)
     * @return GAST in hours (0-24)
     */
    @JvmStatic
    fun calculateGAST(julianDay: JulianDay): Double {
        val gmst = calculateGMST(julianDay)
        val eqEquinoxes = calculateEquationOfEquinoxes(julianDay)
        
        // Equation of equinoxes is in seconds, convert to hours
        val gastHours = gmst + eqEquinoxes / 3600.0
        
        return normalizeHours(gastHours)
    }

    /**
     * Calculates Local Sidereal Time (LST) for a given longitude.
     *
     * LST = GMST + longitude (in hours)
     *
     * @param julianDay Julian Day (UT)
     * @param longitudeDegrees Geographic longitude in degrees (positive East, negative West)
     * @return LST in hours (0-24)
     */
    @JvmStatic
    fun calculateLST(julianDay: JulianDay, longitudeDegrees: Double): Double {
        val gmst = calculateGMST(julianDay)
        val longitudeHours = longitudeDegrees / 15.0 // Convert degrees to hours
        
        return normalizeHours(gmst + longitudeHours)
    }

    /**
     * Calculates Local Apparent Sidereal Time (LAST) for a given longitude.
     *
     * LAST = GAST + longitude (in hours)
     *
     * @param julianDay Julian Day (UT)
     * @param longitudeDegrees Geographic longitude in degrees (positive East, negative West)
     * @return LAST in hours (0-24)
     */
    @JvmStatic
    fun calculateLAST(julianDay: JulianDay, longitudeDegrees: Double): Double {
        val gast = calculateGAST(julianDay)
        val longitudeHours = longitudeDegrees / 15.0
        
        return normalizeHours(gast + longitudeHours)
    }

    /**
     * Calculates the equation of equinoxes (difference between apparent and mean sidereal time).
     *
     * This is caused by nutation of Earth's axis.
     * Formula: Δψ * cos(ε)
     *
     * where:
     * - Δψ = nutation in longitude
     * - ε = mean obliquity of ecliptic
     *
     * @param julianDay Julian Day (UT or TT)
     * @return Equation of equinoxes in seconds of time
     */
    @JvmStatic
    fun calculateEquationOfEquinoxes(julianDay: JulianDay): Double {
        // For now, use simplified calculation
        // Full implementation would require nutation calculations
        // Typical value: 0-1 second
        
        val jd = julianDay.value
        val t = (jd - J2000) / 36525.0 // Centuries from J2000
        
        // Mean obliquity of ecliptic (IAU 1976)
        val epsilon = 23.439291 - 0.0130042 * t
        val epsilonRad = toRadians(epsilon)
        
        // Simplified nutation in longitude (arcseconds)
        // Full calculation would use IAU 2000A/2000B nutation series
        val omega = 125.04 - 0.052954 * (jd - J2000)
        val omegaRad = toRadians(omega)
        val deltaPsi = -17.20 * sin(omegaRad) // Simplified (arcseconds)
        
        // Equation of equinoxes = Δψ * cos(ε) * 4 (convert arcsec to time seconds)
        // 1 arcsecond of RA = 1/15 seconds of time = 0.0667 time seconds
        return deltaPsi * cos(epsilonRad) / 15.0
    }

    /**
     * Converts sidereal time from hours to degrees.
     *
     * 1 hour = 15 degrees
     *
     * @param hours Sidereal time in hours
     * @return Angle in degrees
     */
    @JvmStatic
    fun hoursToDegrees(hours: Double): Double {
        return hours * 15.0
    }

    /**
     * Converts angle from degrees to sidereal hours.
     *
     * 15 degrees = 1 hour
     *
     * @param degrees Angle in degrees
     * @return Sidereal time in hours
     */
    @JvmStatic
    fun degreesToHours(degrees: Double): Double {
        return degrees / 15.0
    }

    /**
     * Converts sidereal time from hours, minutes, seconds to decimal hours.
     *
     * @param hours Hours
     * @param minutes Minutes
     * @param seconds Seconds
     * @return Decimal hours
     */
    @JvmStatic
    fun hmsToHours(hours: Int, minutes: Int, seconds: Double): Double {
        return hours + minutes / 60.0 + seconds / 3600.0
    }

    /**
     * Converts decimal hours to hours, minutes, seconds.
     *
     * @param decimalHours Decimal hours
     * @return Triple of (hours, minutes, seconds)
     */
    @JvmStatic
    fun hoursToHMS(decimalHours: Double): Triple<Int, Int, Double> {
        val normalized = normalizeHours(decimalHours)
        val hours = floor(normalized).toInt()
        val minutesDecimal = (normalized - hours) * 60.0
        val minutes = floor(minutesDecimal).toInt()
        val seconds = (minutesDecimal - minutes) * 60.0
        
        return Triple(hours, minutes, seconds)
    }

    /**
     * Normalizes hours to the range [0, 24).
     *
     * @param hours Hours (can be any value)
     * @return Normalized hours in range [0, 24)
     */
    private fun normalizeHours(hours: Double): Double {
        var normalized = hours % 24.0
        if (normalized < 0.0) normalized += 24.0
        return normalized
    }

    /**
     * Converts degrees to radians.
     */
    private fun toRadians(degrees: Double): Double = degrees * PI / 180.0
}
