package ch.typedef.swekt.time

/**
 * Different astronomical time scales used in calculations.
 *
 * Time scales in astronomy are complex due to Earth's irregular rotation
 * and the need for precise time measurements.
 */
enum class TimeScale {
    /**
     * Universal Time (UT1)
     * Based on Earth's rotation. Irregular due to variations in Earth's rotation speed.
     * Used for sidereal time calculations and observational astronomy.
     */
    UT1,

    /**
     * Universal Time Coordinated (UTC)
     * Civil time standard, kept within 0.9 seconds of UT1 using leap seconds.
     * This is the time used in everyday life and most computer systems.
     */
    UTC,

    /**
     * Terrestrial Time (TT)
     * Uniform time scale for Earth-based observations.
     * TT = TAI + 32.184 seconds
     * Formerly called Terrestrial Dynamical Time (TDT).
     */
    TT,

    /**
     * Barycentric Dynamical Time (TDB)
     * Time scale used for solar system ephemerides.
     * Differs from TT by periodic variations (max ~2 milliseconds)
     * due to relativistic effects of Earth's motion around the solar system barycenter.
     */
    TDB,

    /**
     * International Atomic Time (TAI)
     * Uniform time based on atomic clocks.
     * TAI = UTC + leap_seconds
     * Not used directly in astronomical calculations but forms the basis for TT.
     */
    TAI;

    companion object {
        /**
         * Gets the default time scale for astronomical calculations.
         */
        @JvmStatic
        fun getDefault(): TimeScale = TT
    }
}
