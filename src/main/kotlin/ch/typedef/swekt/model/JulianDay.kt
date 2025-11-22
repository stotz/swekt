package ch.typedef.swekt.model

import kotlin.math.floor

/**
 * Julian Day Number - astronomical day numbering system.
 *
 * The Julian Day starts at Greenwich noon (12:00 UT).
 * J2000.0 epoch = JD 2451545.0 = 2000-01-01 12:00:00 UT
 *
 * This class uses a regular class (not value class) for Java interoperability.
 *
 * @property value The Julian Day number
 */
data class JulianDay(val value: Double) : Comparable<JulianDay> {

    operator fun plus(days: Double): JulianDay = JulianDay(value + days)
    
    operator fun minus(other: JulianDay): Double = value - other.value
    
    override fun compareTo(other: JulianDay): Int = value.compareTo(other.value)

    /**
     * Converts this Julian Day to Gregorian calendar date.
     * Uses proleptic Gregorian calendar (Gregorian rules extended to all dates).
     */
    fun toGregorian(): GregorianDate {
        val jd = value + 0.5
        val z = floor(jd).toInt()
        val f = jd - z
        
        // Always apply Gregorian correction (no Julian calendar cutoff)
        val alpha = floor((z - 1867216.25) / 36524.25).toInt()
        val a = z + 1 + alpha - floor(alpha / 4.0).toInt()
        
        val b = a + 1524
        val c = floor((b - 122.1) / 365.25).toInt()
        val d = floor(365.25 * c).toInt()
        val e = floor((b - d) / 30.6001).toInt()
        
        val day = b - d - floor(30.6001 * e).toInt()
        val month = if (e < 14) e - 1 else e - 13
        val year = if (month > 2) c - 4716 else c - 4715
        
        val hour = f * 24.0
        
        return GregorianDate(year, month, day, hour)
    }

    companion object {
        /**
         * J2000.0 epoch = 2000-01-01 12:00:00 UT
         */
        @JvmField
        val J2000 = JulianDay(2451545.0)

        /**
         * J1900.0 epoch = 1899-12-31 12:00:00 UT
         */
        @JvmField
        val J1900 = JulianDay(2415020.0)

        /**
         * Java getter for J2000
         */
        @JvmStatic
        fun getJ2000(): JulianDay = J2000

        /**
         * Java getter for J1900
         */
        @JvmStatic
        fun getJ1900(): JulianDay = J1900

        /**
         * Creates a Julian Day from Gregorian calendar date.
         *
         * @param year Year (can be negative for BCE)
         * @param month Month (1-12)
         * @param day Day (1-31)
         * @param hour Hour as decimal (0.0-23.999...), default is 12.0 (noon UT)
         * @return JulianDay
         * @throws IllegalArgumentException if date components are invalid
         */
        @JvmStatic
        @JvmOverloads
        fun fromGregorian(
            year: Int,
            month: Int,
            day: Int,
            hour: Double = 12.0
        ): JulianDay {
            require(month in 1..12) { "Month must be between 1 and 12, got: $month" }
            require(day in 1..31) { "Day must be between 1 and 31, got: $day" }
            require(hour in 0.0..<24.0) { "Hour must be between 0.0 and 24.0, got: $hour" }
            
            // Validate day for month
            val maxDay = when (month) {
                2 -> if (isLeapYear(year)) 29 else 28
                4, 6, 9, 11 -> 30
                else -> 31
            }
            require(day <= maxDay) {
                "Invalid day $day for month $month in year $year (max: $maxDay)"
            }
            
            var y = year
            var m = month
            
            if (m <= 2) {
                y -= 1
                m += 12
            }
            
            // Always use proleptic Gregorian calendar (no Julian calendar cutoff)
            val a = floor(y / 100.0).toInt()
            val b = 2 - a + floor(a / 4.0).toInt()
            
            val jd = floor(365.25 * (y + 4716)) +
                    floor(30.6001 * (m + 1)) +
                    day + b - 1524.5 +
                    (hour / 24.0)
            
            return JulianDay(jd)
        }

        @JvmStatic
        fun isLeapYear(year: Int): Boolean {
            return when {
                year % 400 == 0 -> true
                year % 100 == 0 -> false
                year % 4 == 0 -> true
                else -> false
            }
        }
    }
}

/**
 * Gregorian calendar date with time.
 *
 * @property year Year (can be negative for BCE)
 * @property month Month (1-12)
 * @property day Day (1-31)
 * @property hour Hour as decimal (0.0-23.999...)
 */
data class GregorianDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Double = 12.0
) {
    override fun toString(): String {
        val hours = hour.toInt()
        val minutes = ((hour - hours) * 60).toInt()
        val seconds = ((hour - hours - minutes / 60.0) * 3600).toInt()
        
        return String.format(
            "%04d-%02d-%02d %02d:%02d:%02d UT",
            year, month, day, hours, minutes, seconds
        )
    }
}
