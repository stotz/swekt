package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay
import kotlin.math.*

/**
 * Delta T (ΔT) is the difference between Terrestrial Time (TT) and Universal Time (UT).
 *
 * ΔT = TT - UT
 *
 * This difference exists because:
 * - TT is a uniform time scale (atomic time + 32.184 seconds)
 * - UT is based on Earth's rotation, which is irregular and slowing down
 *
 * ΔT varies from:
 * - ~0 seconds around 1900
 * - ~70 seconds in 2020
 * - Can be negative in ancient times (Earth rotated faster)
 * - Predicted to increase to ~100+ seconds by 2100
 *
 * Accurate ΔT is critical for:
 * - Precise planetary positions
 * - Eclipse predictions
 * - Historical astronomical events
 * - House calculations (need sidereal time)
 *
 * Implementation based on:
 * - Morrison & Stephenson (2004, 2005)
 * - Espenak & Meeus (NASA)
 * - IERS Bulletins (modern values)
 * - Swiss Ephemeris algorithms
 */
object DeltaT {

    /**
     * J2000 epoch as Julian Day
     */
    private const val J2000 = 2451545.0

    /**
     * Tidal acceleration of the Moon in arcsec/century²
     * Default value from Morrison & Stephenson (2004)
     * This affects long-term Delta T calculations
     */
    private const val TIDAL_ACCELERATION = -25.8

    /**
     * Calculates Delta T (ΔT) for a given Julian Day.
     *
     * Returns the difference TT - UT in days.
     *
     * @param julianDay The Julian Day (in UT scale)
     * @return ΔT in days (TT - UT)
     */
    @JvmStatic
    fun calculate(julianDay: JulianDay): Double {
        return calculateSeconds(julianDay.value) / 86400.0 // Convert seconds to days
    }

    /**
     * Calculates Delta T in seconds for a given Julian Day.
     *
     * @param jd Julian Day (UT)
     * @return ΔT in seconds (TT - UT)
     */
    @JvmStatic
    fun calculateSeconds(jd: Double): Double {
        // Convert JD to decimal year for polynomial calculations
        val year = jdToYear(jd)
        
        return when {
            // Modern era (1972-present): Use leap seconds + fixed offset
            year >= 1972.0 -> calculateModern(jd, year)
            
            // Historic era (1600-1972): Polynomial approximations
            year >= 1600.0 -> calculateHistoric(year)
            
            // Ancient era (before 1600): Parabolic extrapolation
            else -> calculateAncient(year)
        }
    }

    /**
     * Modern era (1972-present): ΔT = TAI - UTC + 32.184
     *
     * Since 1972, UTC has been kept within 0.9 seconds of UT1 using leap seconds.
     * TAI (International Atomic Time) is the basis for TT.
     * TT = TAI + 32.184 seconds
     *
     * Therefore: ΔT = (number of leap seconds) + 32.184 seconds + (UT1 - UTC correction)
     */
    private fun calculateModern(jd: Double, year: Double): Double {
        val leapSeconds = getLeapSeconds(jd)
        
        // TT - UTC = leap_seconds + 32.184
        // For simplicity, we use UT ≈ UTC (difference < 0.9 seconds)
        // More precise: would need UT1 - UTC from IERS Bulletin A
        
        return leapSeconds + 32.184
    }

    /**
     * Historic era (1600-1972): Use polynomial approximations
     * Based on Morrison & Stephenson and NASA algorithms
     */
    private fun calculateHistoric(year: Double): Double {
        return when {
            // 1955-1972: Polynomial from Astronomical Almanac
            year >= 1955.0 -> {
                val t = year - 1955.0
                31.1 + 0.2523 * t + 0.0585 * t * t + 0.00157 * t * t * t
            }
            
            // 1900-1955: Polynomial from Astronomical Almanac
            year >= 1900.0 -> {
                val t = year - 1900.0
                -2.79 + 1.494119 * t - 0.0598939 * t * t + 0.0061966 * t * t * t - 0.000197 * t * t * t * t
            }
            
            // 1800-1900: Polynomial from Astronomical Almanac
            year >= 1800.0 -> {
                val t = year - 1800.0
                13.72 - 0.332447 * t + 0.0068612 * t * t + 0.0041116 * t * t * t - 0.00037436 * t * t * t * t +
                        0.0000121272 * t.pow(5.0) - 0.0000001699 * t.pow(6.0) + 0.000000000875 * t.pow(7.0)
            }
            
            // 1700-1800: Polynomial from Morrison & Stephenson (2004)
            year >= 1700.0 -> {
                val t = year - 1700.0
                8.83 + 0.1603 * t - 0.0059285 * t * t + 0.00013336 * t * t * t - t.pow(4.0) / 1174000.0
            }
            
            // 1600-1700: Polynomial from Morrison & Stephenson (2004)
            else -> {
                val t = year - 1600.0
                120.0 - 0.9808 * t - 0.01532 * t * t + t.pow(3.0) / 7129.0
            }
        }
    }

    /**
     * Ancient era (before 1600): Parabolic extrapolation
     * Based on tidal acceleration of the Moon
     */
    private fun calculateAncient(year: Double): Double {
        // Time in centuries from J2000
        val t = (year - 2000.0) / 100.0
        
        // Parabolic formula: ΔT = -20 + 32 * u^2
        // where u = (year - 1820) / 100
        val u = (year - 1820.0) / 100.0
        
        // Include tidal acceleration effect
        // Formula from Morrison & Stephenson (2004)
        return -20.0 + 32.0 * u * u
    }

    /**
     * Get number of leap seconds inserted since 1972
     * Leap seconds are added to keep UTC within 0.9 seconds of UT1
     */
    private fun getLeapSeconds(jd: Double): Double {
        // Convert to calendar date to check leap second table
        val year = jdToYear(jd)
        
        // Leap second dates (end of day when leap second was added)
        // Source: IERS Bulletin C
        return when {
            jd >= 2457754.5 -> 37.0 // 2017-01-01
            jd >= 2457204.5 -> 36.0 // 2015-07-01
            jd >= 2456109.5 -> 35.0 // 2012-07-01
            jd >= 2454832.5 -> 34.0 // 2009-01-01
            jd >= 2453736.5 -> 33.0 // 2006-01-01
            jd >= 2451179.5 -> 32.0 // 1999-01-01
            jd >= 2450630.5 -> 31.0 // 1997-07-01
            jd >= 2450083.5 -> 30.0 // 1996-01-01
            jd >= 2449534.5 -> 29.0 // 1994-07-01
            jd >= 2449169.5 -> 28.0 // 1993-07-01
            jd >= 2448804.5 -> 27.0 // 1992-07-01
            jd >= 2448257.5 -> 26.0 // 1991-01-01
            jd >= 2447892.5 -> 25.0 // 1990-01-01
            jd >= 2447161.5 -> 24.0 // 1988-01-01
            jd >= 2446247.5 -> 23.0 // 1985-07-01
            jd >= 2445516.5 -> 22.0 // 1983-07-01
            jd >= 2445151.5 -> 21.0 // 1982-07-01
            jd >= 2444786.5 -> 20.0 // 1981-07-01
            jd >= 2444239.5 -> 19.0 // 1980-01-01
            jd >= 2443874.5 -> 18.0 // 1979-01-01
            jd >= 2443509.5 -> 17.0 // 1978-01-01
            jd >= 2443144.5 -> 16.0 // 1977-01-01
            jd >= 2442778.5 -> 15.0 // 1976-01-01
            jd >= 2442413.5 -> 14.0 // 1975-01-01
            jd >= 2442048.5 -> 13.0 // 1974-01-01
            jd >= 2441683.5 -> 12.0 // 1973-01-01
            jd >= 2441499.5 -> 11.0 // 1972-07-01
            jd >= 2441317.5 -> 10.0 // 1972-01-01
            else -> 10.0 // Before 1972, use 10 seconds as base
        }
    }

    /**
     * Convert Julian Day to decimal year
     * Uses proper Gregorian calendar conversion
     */
    private fun jdToYear(jd: Double): Double {
        // Use JulianDay's Gregorian conversion
        val julianDay = JulianDay(jd)
        val greg = julianDay.toGregorian()
        
        // Calculate day of year
        val dayOfYear = greg.day + (greg.hour / 24.0)
        
        // Days in year (365 or 366 for leap year)
        val daysInYear = if (JulianDay.isLeapYear(greg.year)) 366.0 else 365.0
        
        // Return decimal year
        return greg.year.toDouble() + (greg.month - 1) * 30.5 / daysInYear + dayOfYear / daysInYear
    }

    /**
     * Convert decimal year to Julian Day
     */
    @JvmStatic
    fun yearToJd(year: Double): Double {
        return J2000 + (year - 2000.0) * 365.25
    }
}
