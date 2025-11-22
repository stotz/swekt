package ch.typedef.swekt.time

import ch.typedef.swekt.model.JulianDay

/**
 * Utilities for converting between different astronomical time scales.
 *
 * Time Scale Relationships:
 * - TT = TAI + 32.184 seconds
 * - ΔT = TT - UT
 * - TT = UT + ΔT
 * - UTC ≈ UT (kept within 0.9 seconds using leap seconds)
 * - TDB ≈ TT (differs by periodic terms, max ~2 milliseconds)
 *
 * Typical workflow:
 * 1. User provides UTC time
 * 2. Convert UTC → UT (usually UT ≈ UTC for modern dates)
 * 3. Convert UT → TT using ΔT
 * 4. Use TT for calculations
 * 5. Optionally convert TT → TDB for solar system barycentric calculations
 */
object TimeConversion {

    /**
     * Converts Universal Time (UT) to Terrestrial Time (TT).
     *
     * TT = UT + ΔT
     *
     * @param utJd Julian Day in UT scale
     * @return Julian Day in TT scale
     */
    @JvmStatic
    fun utToTT(utJd: JulianDay): JulianDay {
        val deltaT = DeltaT.calculate(utJd)
        return JulianDay(utJd.value + deltaT)
    }

    /**
     * Converts Terrestrial Time (TT) to Universal Time (UT).
     *
     * UT = TT - ΔT
     *
     * Note: This requires iteration because ΔT is a function of UT.
     *
     * @param ttJd Julian Day in TT scale
     * @return Julian Day in UT scale
     */
    @JvmStatic
    fun ttToUT(ttJd: JulianDay): JulianDay {
        // Initial guess: UT ≈ TT - ΔT(TT)
        var utGuess = ttJd.value - DeltaT.calculate(ttJd)
        
        // Iterate to refine (usually converges in 2-3 iterations)
        for (i in 0 until 5) {
            val deltaT = DeltaT.calculate(JulianDay(utGuess))
            val newUtGuess = ttJd.value - deltaT
            
            // Check convergence (0.001 seconds = ~1e-8 days)
            if (abs(newUtGuess - utGuess) < 1e-8) {
                return JulianDay(newUtGuess)
            }
            
            utGuess = newUtGuess
        }
        
        return JulianDay(utGuess)
    }

    /**
     * Converts UTC to Terrestrial Time (TT).
     *
     * For modern dates (post-1972): TT ≈ UTC + leap_seconds + 32.184 seconds
     * For historic dates: Uses UT ≈ UTC approximation, then applies ΔT
     *
     * @param utcJd Julian Day in UTC scale
     * @return Julian Day in TT scale
     */
    @JvmStatic
    fun utcToTT(utcJd: JulianDay): JulianDay {
        // UTC ≈ UT for our purposes (difference < 0.9 seconds)
        // For more precision, would need UT1 - UTC from IERS Bulletin A
        return utToTT(utcJd)
    }

    /**
     * Converts Terrestrial Time (TT) to UTC.
     *
     * @param ttJd Julian Day in TT scale
     * @return Julian Day in UTC scale
     */
    @JvmStatic
    fun ttToUTC(ttJd: JulianDay): JulianDay {
        // UTC ≈ UT for our purposes
        return ttToUT(ttJd)
    }

    /**
     * Converts Terrestrial Time (TT) to Barycentric Dynamical Time (TDB).
     *
     * TDB differs from TT by periodic terms due to relativistic effects
     * of Earth's motion around the solar system barycenter.
     *
     * Maximum difference: ~2 milliseconds
     *
     * Formula from Fairhead & Bretagnon (1990):
     * TDB - TT = 0.001658 * sin(g) + 0.000014 * sin(2g)
     * where g = 357.53 + 0.98560028 * (JD - 2451545.0) degrees
     *
     * @param ttJd Julian Day in TT scale
     * @return Julian Day in TDB scale
     */
    @JvmStatic
    fun ttToTDB(ttJd: JulianDay): JulianDay {
        val jd = ttJd.value
        
        // Days from J2000.0
        val t = jd - 2451545.0
        
        // Mean anomaly of the Sun (in degrees)
        val g = (357.53 + 0.98560028 * t) % 360.0
        val gRad = Math.toRadians(g)
        
        // TDB - TT in seconds
        val deltaTDB = 0.001658 * kotlin.math.sin(gRad) + 
                       0.000014 * kotlin.math.sin(2.0 * gRad)
        
        // Convert seconds to days and add to TT
        return JulianDay(jd + deltaTDB / 86400.0)
    }

    /**
     * Converts Barycentric Dynamical Time (TDB) to Terrestrial Time (TT).
     *
     * @param tdbJd Julian Day in TDB scale
     * @return Julian Day in TT scale
     */
    @JvmStatic
    fun tdbToTT(tdbJd: JulianDay): JulianDay {
        // Use same formula but subtract instead of add
        val jd = tdbJd.value
        val t = jd - 2451545.0
        val g = (357.53 + 0.98560028 * t) % 360.0
        val gRad = Math.toRadians(g)
        
        val deltaTDB = 0.001658 * kotlin.math.sin(gRad) + 
                       0.000014 * kotlin.math.sin(2.0 * gRad)
        
        return JulianDay(jd - deltaTDB / 86400.0)
    }

    /**
     * Converts Universal Time (UT) to Barycentric Dynamical Time (TDB).
     *
     * TDB = TT + (periodic terms)
     * TT = UT + ΔT
     * Therefore: TDB = UT + ΔT + (periodic terms)
     *
     * @param utJd Julian Day in UT scale
     * @return Julian Day in TDB scale
     */
    @JvmStatic
    fun utToTDB(utJd: JulianDay): JulianDay {
        val ttJd = utToTT(utJd)
        return ttToTDB(ttJd)
    }

    /**
     * Converts Barycentric Dynamical Time (TDB) to Universal Time (UT).
     *
     * @param tdbJd Julian Day in TDB scale
     * @return Julian Day in UT scale
     */
    @JvmStatic
    fun tdbToUT(tdbJd: JulianDay): JulianDay {
        val ttJd = tdbToTT(tdbJd)
        return ttToUT(ttJd)
    }

    /**
     * Gets Delta T (ΔT) for a given UT Julian Day.
     *
     * ΔT = TT - UT
     *
     * @param utJd Julian Day in UT scale
     * @return ΔT in days
     */
    @JvmStatic
    fun getDeltaT(utJd: JulianDay): Double {
        return DeltaT.calculate(utJd)
    }

    /**
     * Gets Delta T in seconds for a given UT Julian Day.
     *
     * @param utJd Julian Day in UT scale
     * @return ΔT in seconds
     */
    @JvmStatic
    fun getDeltaTSeconds(utJd: JulianDay): Double {
        return DeltaT.calculateSeconds(utJd.value)
    }

    /**
     * Helper for abs function
     */
    private fun abs(x: Double): Double = kotlin.math.abs(x)
}
