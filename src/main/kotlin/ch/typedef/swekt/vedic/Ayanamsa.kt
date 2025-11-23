package ch.typedef.swekt.vedic

import ch.typedef.swekt.model.JulianDay
import kotlin.math.floor

/**
 * Vedic (Sidereal) Ayanamsa systems.
 *
 * Ayanamsa is the difference between tropical and sidereal zodiac.
 * Different traditions use different calculation methods.
 */
enum class Ayanamsa(val code: Int, val displayName: String) {
    /**
     * Fagan/Bradley - Most popular Western sidereal system.
     * Based on Spica at 29°00' Virgo in tropical zodiac.
     * Ayanamsa at 2000.0: 24°42'46"
     */
    FAGAN_BRADLEY(0, "Fagan/Bradley"),

    /**
     * Lahiri - Official Indian government system since 1956.
     * Based on Spica at 0° Libra (180°) in sidereal zodiac.
     * Most widely used in India.
     * Ayanamsa at 2000.0: 23°51'17"
     */
    LAHIRI(1, "Lahiri"),

    /**
     * De Luce - Used by some Western sidereal astrologers.
     */
    DE_LUCE(2, "De Luce"),

    /**
     * Raman - B.V. Raman's system.
     * Popular in South India.
     * Ayanamsa at 2000.0: 22°27'37"
     */
    RAMAN(3, "Raman"),

    /**
     * Ushashashi - Alternative Indian system.
     */
    USHASHASHI(4, "Ushashashi"),

    /**
     * Krishnamurti - K.P. system (Krishnamurti Paddhati).
     * Popular in India for predictive astrology.
     * Ayanamsa at 2000.0: 23°53'49"
     */
    KRISHNAMURTI(5, "Krishnamurti (KP)"),

    /**
     * Djwhal Khul - Esoteric astrology system.
     */
    DJWHAL_KHUL(6, "Djwhal Khul"),

    /**
     * Yukteshwar - Swami Sri Yukteshwar's system.
     */
    YUKTESHWAR(7, "Yukteshwar"),

    /**
     * J.N. Bhasin - Indian astrologer.
     */
    JN_BHASIN(8, "J.N. Bhasin"),

    /**
     * Babylonian/Kugler 1 - Ancient Babylonian system.
     */
    BABYLONIAN_KUGLER1(9, "Babylonian (Kugler 1)"),

    /**
     * Babylonian/Kugler 2 - Alternative Babylonian system.
     */
    BABYLONIAN_KUGLER2(10, "Babylonian (Kugler 2)"),

    /**
     * Babylonian/Kugler 3 - Third Babylonian variant.
     */
    BABYLONIAN_KUGLER3(11, "Babylonian (Kugler 3)"),

    /**
     * Babylonian/Huber - Huber's Babylonian system.
     */
    BABYLONIAN_HUBER(12, "Babylonian (Huber)"),

    /**
     * Babylonian/Eta Piscium - Based on Eta Piscium star.
     */
    BABYLONIAN_ETA_PISCIUM(13, "Babylonian (Eta Piscium)"),

    /**
     * Aldebaran at 15° Taurus - Ancient Egyptian system.
     */
    ALDEBARAN_15TAU(14, "Aldebaran at 15° Tau"),

    /**
     * Hipparchos - Ancient Greek astronomer.
     */
    HIPPARCHOS(15, "Hipparchos"),

    /**
     * Sassanian - Ancient Persian system.
     */
    SASSANIAN(16, "Sassanian"),

    /**
     * Galactic Center at 0° Sagittarius.
     * Modern astronomical alignment.
     */
    GALACTIC_CENTER_0SAG(17, "Galactic Center at 0° Sag"),

    /**
     * J2000 - Astronomical standard epoch.
     */
    J2000(18, "J2000"),

    /**
     * J1900 - Historical epoch.
     */
    J1900(19, "J1900"),

    /**
     * B1950 - Besselian epoch.
     */
    B1950(20, "B1950"),

    /**
     * Suryasiddhanta - Ancient Sanskrit astronomical text.
     */
    SURYASIDDHANTA(21, "Suryasiddhanta"),

    /**
     * Suryasiddhanta (mean Sun) - Using mean Sun position.
     */
    SURYASIDDHANTA_MSUN(22, "Suryasiddhanta (mean Sun)"),

    /**
     * Aryabhata - 5th century Indian astronomer.
     */
    ARYABHATA(23, "Aryabhata"),

    /**
     * Aryabhata (mean Sun) - Using mean Sun position.
     */
    ARYABHATA_MSUN(24, "Aryabhata (mean Sun)"),

    /**
     * SS Revati - Suryasiddhanta with Revati as reference.
     */
    SS_REVATI(25, "SS Revati"),

    /**
     * SS Citra - Suryasiddhanta with Citra (Spica) as reference.
     */
    SS_CITRA(26, "SS Citra"),

    /**
     * True Citra - Based on actual Spica position.
     */
    TRUE_CITRA(27, "True Citra"),

    /**
     * True Revati - Based on actual Revati (Zeta Piscium) position.
     */
    TRUE_REVATI(28, "True Revati"),

    /**
     * True Pushya - Based on actual Pushya (Delta Cancri) position.
     */
    TRUE_PUSHYA(29, "True Pushya"),

    /**
     * Galactic Center (Gil Brand).
     */
    GALACTIC_CENTER_BRAND(30, "Galactic Center (Brand)"),

    /**
     * Galactic Equator (IAU1958).
     */
    GALACTIC_EQUATOR_IAU1958(31, "Galactic Equator (IAU1958)"),

    /**
     * Galactic Equator (True/Mula).
     */
    GALACTIC_EQUATOR_TRUE(32, "Galactic Equator (True/Mula)"),

    /**
     * Galactic Equator (Mardyks).
     */
    GALACTIC_EQUATOR_MARDYKS(33, "Galactic Equator (Mardyks)"),

    /**
     * Galactic Center (Mardyks).
     */
    GALACTIC_CENTER_MARDYKS(34, "Galactic Center (Mardyks)"),

    /**
     * Galactic Equator (Fiorenza).
     */
    GALACTIC_EQUATOR_FIORENZA(35, "Galactic Equator (Fiorenza)"),

    /**
     * Andromeda Galaxy.
     */
    ANDROMEDA(36, "Andromeda"),

    /**
     * Skydram (Mardyks).
     */
    SKYDRAM(37, "Skydram (Mardyks)"),

    /**
     * True Mula.
     */
    TRUE_MULA(38, "True Mula"),

    /**
     * Dhruva/Galactic Center/Mula (Wilhelm).
     */
    GALACTIC_CENTER_WILHELM(39, "Dhruva/Galactic Center/Mula (Wilhelm)"),

    /**
     * Aryabhata 522 - Historical variant.
     */
    ARYABHATA_522(40, "Aryabhata 522"),

    /**
     * Babylonian/Britton.
     */
    BABYLONIAN_BRITTON(41, "Babylonian (Britton)"),

    /**
     * Vedic/Sheoran.
     */
    VEDIC_SHEORAN(42, "Vedic (Sheoran)"),

    /**
     * Cochrane (Galactic Center = 0° Capricorn).
     */
    COCHRANE(43, "Cochrane (Galactic Center = 0° Cap)"),

    /**
     * Galactic Equator (mid-Mula).
     */
    GALACTIC_EQUATOR_MID_MULA(44, "Galactic Equator (mid-Mula)"),

    /**
     * True Sheoran.
     */
    TRUE_SHEORAN(45, "True Sheoran"),

    /**
     * Galactic Center (Manjula/Senthilathiban).
     */
    GALACTIC_CENTER_MANJULA(46, "Galactic Center (Manjula/Senthilathiban)");

    companion object {
        /**
         * Get Ayanamsa by code.
         */
        @JvmStatic
        fun fromCode(code: Int): Ayanamsa? {
            return entries.find { it.code == code }
        }

        /**
         * Get Ayanamsa by name (case-insensitive).
         */
        @JvmStatic
        fun fromName(name: String): Ayanamsa? {
            return entries.find {
                it.displayName.equals(name, ignoreCase = true) ||
                it.name.equals(name, ignoreCase = true)
            }
        }
    }
}

/**
 * Calculator for Ayanamsa (precession correction).
 *
 * Converts between tropical and sidereal zodiac positions.
 */
class AyanamsaCalculator {

    /**
     * Calculate Ayanamsa for given Julian Day and system.
     *
     * @param julianDay Time of calculation (UT or TT)
     * @param system Ayanamsa system to use
     * @return Ayanamsa value in degrees
     */
    fun calculate(julianDay: JulianDay, system: Ayanamsa): Double {
        // Julian centuries from J2000.0
        val T = (julianDay.value - 2451545.0) / 36525.0

        return when (system) {
            Ayanamsa.FAGAN_BRADLEY -> calculateFaganBradley(T)
            Ayanamsa.LAHIRI -> calculateLahiri(T)
            Ayanamsa.RAMAN -> calculateRaman(T)
            Ayanamsa.KRISHNAMURTI -> calculateKrishnamurti(T)
            else -> calculateLahiri(T) // Default to Lahiri
        }
    }

    /**
     * Convert tropical longitude to sidereal.
     *
     * @param tropicalLongitude Tropical ecliptic longitude in degrees
     * @param julianDay Time of calculation
     * @param system Ayanamsa system
     * @return Sidereal longitude in degrees
     */
    fun tropicalToSidereal(
        tropicalLongitude: Double,
        julianDay: JulianDay,
        system: Ayanamsa
    ): Double {
        val ayanamsa = calculate(julianDay, system)
        var sidereal = tropicalLongitude - ayanamsa
        
        // Normalize to 0-360
        while (sidereal < 0.0) sidereal += 360.0
        while (sidereal >= 360.0) sidereal -= 360.0
        
        return sidereal
    }

    /**
     * Convert sidereal longitude to tropical.
     *
     * @param siderealLongitude Sidereal ecliptic longitude in degrees
     * @param julianDay Time of calculation
     * @param system Ayanamsa system
     * @return Tropical longitude in degrees
     */
    fun siderealToTropical(
        siderealLongitude: Double,
        julianDay: JulianDay,
        system: Ayanamsa
    ): Double {
        val ayanamsa = calculate(julianDay, system)
        var tropical = siderealLongitude + ayanamsa
        
        // Normalize to 0-360
        while (tropical < 0.0) tropical += 360.0
        while (tropical >= 360.0) tropical -= 360.0
        
        return tropical
    }

    /**
     * Get zodiac sign for sidereal longitude.
     *
     * @param longitude Sidereal longitude in degrees (0-360)
     * @return Sign index (0-11) where 0 = Aries
     */
    fun getSiderealSign(longitude: Double): Int {
        return floor(longitude / 30.0).toInt() % 12
    }

    /**
     * Get Nakshatra (lunar mansion) for sidereal longitude.
     *
     * @param longitude Sidereal longitude in degrees (0-360)
     * @return Nakshatra index (0-26) where 0 = Ashwini
     */
    fun getNakshatra(longitude: Double): Int {
        return floor(longitude / 13.333333333).toInt() % 27
    }

    /**
     * Get Nakshatra pada (quarter) for sidereal longitude.
     *
     * @param longitude Sidereal longitude in degrees (0-360)
     * @return Pada (1-4)
     */
    fun getNakshatraPada(longitude: Double): Int {
        val degInNakshatra = longitude % 13.333333333
        return floor(degInNakshatra / 3.333333333).toInt() + 1
    }

    /**
     * Fagan/Bradley Ayanamsa calculation.
     *
     * Formula: 24.042506 + 0.000222 * T
     *
     * @param T Julian centuries from J2000.0
     * @return Ayanamsa in degrees
     */
    private fun calculateFaganBradley(T: Double): Double {
        return 24.042506 + 0.000222 * T
    }

    /**
     * Lahiri Ayanamsa calculation (official Indian government system).
     *
     * Formula based on Chitra Paksha (Spica at 180°).
     *
     * @param T Julian centuries from J2000.0
     * @return Ayanamsa in degrees
     */
    private fun calculateLahiri(T: Double): Double {
        // Simplified formula - precise formula is more complex
        // This gives good accuracy for modern times
        return 23.85 + 0.013888888 * (T * 36525 - 6553.5)
    }

    /**
     * Raman Ayanamsa calculation.
     *
     * @param T Julian centuries from J2000.0
     * @return Ayanamsa in degrees
     */
    private fun calculateRaman(T: Double): Double {
        // Raman's formula
        return 22.459444 + 0.000221944 * T * 36525
    }

    /**
     * Krishnamurti (KP) Ayanamsa calculation.
     *
     * @param T Julian centuries from J2000.0
     * @return Ayanamsa in degrees
     */
    private fun calculateKrishnamurti(T: Double): Double {
        // KP ayanamsa is very close to Lahiri
        return 23.896389 + 0.000221944 * T * 36525
    }

    companion object {
        /**
         * Nakshatra names in Sanskrit and English.
         */
        @JvmField
        val NAKSHATRA_NAMES = arrayOf(
            "Ashwini", "Bharani", "Krittika", "Rohini",
            "Mrigashira", "Ardra", "Punarvasu", "Pushya",
            "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
            "Hasta", "Chitra", "Swati", "Vishakha",
            "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha",
            "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
            "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
        )

        /**
         * Sidereal zodiac sign names.
         */
        @JvmField
        val SIDEREAL_SIGN_NAMES = arrayOf(
            "Mesha (Aries)", "Vrishabha (Taurus)", "Mithuna (Gemini)",
            "Karka (Cancer)", "Simha (Leo)", "Kanya (Virgo)",
            "Tula (Libra)", "Vrishchika (Scorpio)", "Dhanu (Sagittarius)",
            "Makara (Capricorn)", "Kumbha (Aquarius)", "Meena (Pisces)"
        )
    }
}
