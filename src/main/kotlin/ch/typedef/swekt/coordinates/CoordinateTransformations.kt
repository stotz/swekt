package ch.typedef.swekt.coordinates

import ch.typedef.swekt.model.CartesianCoordinates
import kotlin.math.*

/**
 * Coordinate system transformations for astronomical calculations.
 *
 * This object provides conversions between different coordinate systems:
 * - Cartesian (x, y, z)
 * - Ecliptic (λ, β, r)
 * - Equatorial (α, δ, r)
 * - Horizontal (Az, Alt, r)
 *
 * Reference: "Astronomical Algorithms" by Jean Meeus
 */
object CoordinateTransformations {

    /**
     * Mean obliquity of the ecliptic (ε) at J2000.0.
     * This is the angle between the ecliptic and equatorial planes.
     * Value: 23.439281° (IAU 2000 model)
     */
    const val OBLIQUITY_J2000 = 23.439281  // degrees

    // ====================
    // Cartesian ↔ Ecliptic
    // ====================

    /**
     * Converts Cartesian coordinates to ecliptic coordinates.
     *
     * @param cartesian Cartesian coordinates (x, y, z) in km
     * @return Ecliptic coordinates (λ, β, r)
     */
    @JvmStatic
    fun cartesianToEcliptic(cartesian: CartesianCoordinates): EclipticCoordinates {
        val x = cartesian.x
        val y = cartesian.y
        val z = cartesian.z

        // Distance r = sqrt(x² + y² + z²)
        val r = sqrt(x * x + y * y + z * z)

        if (r == 0.0) {
            return EclipticCoordinates(0.0, 0.0, 0.0)
        }

        // Longitude λ = atan2(y, x)
        val longitude = Math.toDegrees(atan2(y, x))
        val normalizedLon = ((longitude % 360.0) + 360.0) % 360.0

        // Latitude β = asin(z / r)
        val latitude = Math.toDegrees(asin((z / r).coerceIn(-1.0, 1.0)))

        return EclipticCoordinates(normalizedLon, latitude, r)
    }

    /**
     * Converts ecliptic coordinates to Cartesian coordinates.
     *
     * @param ecliptic Ecliptic coordinates (λ, β, r)
     * @return Cartesian coordinates (x, y, z) in km
     */
    @JvmStatic
    fun eclipticToCartesian(ecliptic: EclipticCoordinates): CartesianCoordinates {
        val lonRad = Math.toRadians(ecliptic.longitude)
        val latRad = Math.toRadians(ecliptic.latitude)
        val r = ecliptic.distance

        val x = r * cos(latRad) * cos(lonRad)
        val y = r * cos(latRad) * sin(lonRad)
        val z = r * sin(latRad)

        return CartesianCoordinates(x, y, z)
    }

    // =======================
    // Cartesian ↔ Equatorial
    // =======================

    /**
     * Converts Cartesian coordinates to equatorial coordinates.
     *
     * Assumes Cartesian coordinates are already in the equatorial frame
     * (x = vernal equinox direction, z = north celestial pole).
     *
     * @param cartesian Cartesian coordinates (x, y, z) in km
     * @return Equatorial coordinates (α, δ, r)
     */
    @JvmStatic
    fun cartesianToEquatorial(cartesian: CartesianCoordinates): EquatorialCoordinates {
        val x = cartesian.x
        val y = cartesian.y
        val z = cartesian.z

        // Distance r = sqrt(x² + y² + z²)
        val r = sqrt(x * x + y * y + z * z)

        if (r == 0.0) {
            return EquatorialCoordinates(0.0, 0.0, 0.0)
        }

        // Right ascension α = atan2(y, x)
        val raDeg = Math.toDegrees(atan2(y, x))
        val normalizedRA = ((raDeg % 360.0) + 360.0) % 360.0
        val raHours = normalizedRA / 15.0  // Convert degrees to hours

        // Declination δ = asin(z / r)
        val declination = Math.toDegrees(asin((z / r).coerceIn(-1.0, 1.0)))

        return EquatorialCoordinates(raHours, declination, r)
    }

    /**
     * Converts equatorial coordinates to Cartesian coordinates.
     *
     * @param equatorial Equatorial coordinates (α, δ, r)
     * @return Cartesian coordinates (x, y, z) in km
     */
    @JvmStatic
    fun equatorialToCartesian(equatorial: EquatorialCoordinates): CartesianCoordinates {
        val raRad = Math.toRadians(equatorial.rightAscensionDegrees())
        val decRad = Math.toRadians(equatorial.declination)
        val r = equatorial.distance

        val x = r * cos(decRad) * cos(raRad)
        val y = r * cos(decRad) * sin(raRad)
        val z = r * sin(decRad)

        return CartesianCoordinates(x, y, z)
    }

    // ========================
    // Ecliptic ↔ Equatorial
    // ========================

    /**
     * Converts ecliptic coordinates to equatorial coordinates.
     *
     * @param ecliptic Ecliptic coordinates (λ, β, r)
     * @param obliquity Obliquity of the ecliptic in degrees (default: J2000.0 value)
     * @return Equatorial coordinates (α, δ, r)
     */
    @JvmStatic
    @JvmOverloads
    fun eclipticToEquatorial(
        ecliptic: EclipticCoordinates,
        obliquity: Double = OBLIQUITY_J2000
    ): EquatorialCoordinates {
        val lonRad = Math.toRadians(ecliptic.longitude)
        val latRad = Math.toRadians(ecliptic.latitude)
        val epsRad = Math.toRadians(obliquity)

        // Right ascension
        val raRad = atan2(
            sin(lonRad) * cos(epsRad) - tan(latRad) * sin(epsRad),
            cos(lonRad)
        )
        val raDeg = Math.toDegrees(raRad)
        val normalizedRA = ((raDeg % 360.0) + 360.0) % 360.0
        val raHours = normalizedRA / 15.0

        // Declination
        val decRad = asin(
            sin(latRad) * cos(epsRad) + cos(latRad) * sin(epsRad) * sin(lonRad)
        )
        val declination = Math.toDegrees(decRad)

        return EquatorialCoordinates(raHours, declination, ecliptic.distance)
    }

    /**
     * Converts equatorial coordinates to ecliptic coordinates.
     *
     * @param equatorial Equatorial coordinates (α, δ, r)
     * @param obliquity Obliquity of the ecliptic in degrees (default: J2000.0 value)
     * @return Ecliptic coordinates (λ, β, r)
     */
    @JvmStatic
    @JvmOverloads
    fun equatorialToEcliptic(
        equatorial: EquatorialCoordinates,
        obliquity: Double = OBLIQUITY_J2000
    ): EclipticCoordinates {
        val raRad = Math.toRadians(equatorial.rightAscensionDegrees())
        val decRad = Math.toRadians(equatorial.declination)
        val epsRad = Math.toRadians(obliquity)

        // Longitude
        val lonRad = atan2(
            sin(raRad) * cos(epsRad) + tan(decRad) * sin(epsRad),
            cos(raRad)
        )
        val longitude = Math.toDegrees(lonRad)
        val normalizedLon = ((longitude % 360.0) + 360.0) % 360.0

        // Latitude
        val latRad = asin(
            sin(decRad) * cos(epsRad) - cos(decRad) * sin(epsRad) * sin(raRad)
        )
        val latitude = Math.toDegrees(latRad)

        return EclipticCoordinates(normalizedLon, latitude, equatorial.distance)
    }

    // ===========================
    // Equatorial → Horizontal
    // ===========================

    /**
     * Converts equatorial coordinates to horizontal coordinates for an observer.
     *
     * @param equatorial Equatorial coordinates (α, δ, r)
     * @param observerLatitude Observer's geographic latitude in degrees (+ north, - south)
     * @param localSiderealTime Local Sidereal Time in hours (0-24)
     * @return Horizontal coordinates (Az, Alt, r)
     */
    @JvmStatic
    fun equatorialToHorizontal(
        equatorial: EquatorialCoordinates,
        observerLatitude: Double,
        localSiderealTime: Double
    ): HorizontalCoordinates {
        val raHours = equatorial.rightAscension
        val decDeg = equatorial.declination
        val r = equatorial.distance

        // Hour angle H = LST - RA
        val hourAngle = (localSiderealTime - raHours) * 15.0  // Convert to degrees
        val H = Math.toRadians(hourAngle)
        val dec = Math.toRadians(decDeg)
        val lat = Math.toRadians(observerLatitude)

        // Altitude
        val altRad = asin(
            sin(lat) * sin(dec) + cos(lat) * cos(dec) * cos(H)
        )
        val altitude = Math.toDegrees(altRad)

        // Azimuth (measured from North through East)
        val azRad = atan2(
            sin(H),
            cos(H) * sin(lat) - tan(dec) * cos(lat)
        )
        val azimuth = (Math.toDegrees(azRad) + 180.0) % 360.0

        return HorizontalCoordinates(azimuth, altitude, r)
    }

    // ========================
    // Convenience Functions
    // ========================

    /**
     * Converts Cartesian coordinates to horizontal coordinates.
     *
     * This is a convenience function that chains:
     * Cartesian → Equatorial → Horizontal
     *
     * @param cartesian Cartesian coordinates (x, y, z) in equatorial frame
     * @param observerLatitude Observer's geographic latitude in degrees
     * @param localSiderealTime Local Sidereal Time in hours
     * @return Horizontal coordinates (Az, Alt, r)
     */
    @JvmStatic
    fun cartesianToHorizontal(
        cartesian: CartesianCoordinates,
        observerLatitude: Double,
        localSiderealTime: Double
    ): HorizontalCoordinates {
        val equatorial = cartesianToEquatorial(cartesian)
        return equatorialToHorizontal(equatorial, observerLatitude, localSiderealTime)
    }

    /**
     * Converts ecliptic coordinates to horizontal coordinates.
     *
     * This chains: Ecliptic → Equatorial → Horizontal
     *
     * @param ecliptic Ecliptic coordinates (λ, β, r)
     * @param observerLatitude Observer's geographic latitude in degrees
     * @param localSiderealTime Local Sidereal Time in hours
     * @param obliquity Obliquity of the ecliptic in degrees (default: J2000.0)
     * @return Horizontal coordinates (Az, Alt, r)
     */
    @JvmStatic
    @JvmOverloads
    fun eclipticToHorizontal(
        ecliptic: EclipticCoordinates,
        observerLatitude: Double,
        localSiderealTime: Double,
        obliquity: Double = OBLIQUITY_J2000
    ): HorizontalCoordinates {
        val equatorial = eclipticToEquatorial(ecliptic, obliquity)
        return equatorialToHorizontal(equatorial, observerLatitude, localSiderealTime)
    }
}
