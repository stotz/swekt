package ch.typedef.swekt.model

import ch.typedef.swekt.coordinates.*

/**
 * Cartesian coordinates in 3D space.
 * 
 * Used for planetary positions and velocities in rectangular coordinate system.
 * Units: typically kilometers for position, km/day for velocity.
 *
 * @property x X-coordinate (positive towards vernal equinox)
 * @property y Y-coordinate (positive towards 90° from vernal equinox)
 * @property z Z-coordinate (positive towards north ecliptic/celestial pole)
 */
data class CartesianCoordinates(
    val x: Double,
    val y: Double,
    val z: Double
) {
    /**
     * Calculates the magnitude (distance from origin).
     */
    fun magnitude(): Double = kotlin.math.sqrt(x * x + y * y + z * z)
    
    /**
     * Adds two coordinate vectors (useful for relative positions).
     */
    operator fun plus(other: CartesianCoordinates): CartesianCoordinates =
        CartesianCoordinates(x + other.x, y + other.y, z + other.z)
    
    /**
     * Subtracts two coordinate vectors (e.g., planet - earth = geocentric).
     */
    operator fun minus(other: CartesianCoordinates): CartesianCoordinates =
        CartesianCoordinates(x - other.x, y - other.y, z - other.z)
    
    /**
     * Scales coordinates by a factor.
     */
    operator fun times(factor: Double): CartesianCoordinates =
        CartesianCoordinates(x * factor, y * factor, z * factor)
    
    /**
     * Returns a normalized unit vector.
     */
    fun normalize(): CartesianCoordinates {
        val mag = magnitude()
        return if (mag > 0) CartesianCoordinates(x / mag, y / mag, z / mag)
        else this
    }

    /**
     * Converts to ecliptic coordinates.
     */
    fun toEcliptic(): EclipticCoordinates =
        CoordinateTransformations.cartesianToEcliptic(this)

    /**
     * Converts to equatorial coordinates.
     */
    fun toEquatorial(): EquatorialCoordinates =
        CoordinateTransformations.cartesianToEquatorial(this)

    /**
     * Converts to horizontal coordinates for an observer.
     *
     * @param observerLatitude Observer's latitude in degrees
     * @param localSiderealTime Local Sidereal Time in hours
     */
    fun toHorizontal(observerLatitude: Double, localSiderealTime: Double): HorizontalCoordinates =
        CoordinateTransformations.cartesianToHorizontal(this, observerLatitude, localSiderealTime)
}

/**
 * Coordinate reference frame.
 */
enum class CoordinateReferenceFrame {
    /**
     * International Celestial Reference Frame (ICRF).
     * Modern standard reference frame aligned with quasars.
     */
    ICRF,
    
    /**
     * J2000.0 mean equator and equinox.
     * Traditional reference frame for epoch J2000.0.
     */
    J2000,
    
    /**
     * True equator and equinox of date.
     * Includes precession and nutation.
     */
    TRUE_OF_DATE,
    
    /**
     * Mean equator and equinox of date.
     * Includes precession only.
     */
    MEAN_OF_DATE,
    
    /**
     * Ecliptic coordinate system.
     */
    ECLIPTIC
}

/**
 * Type of coordinates (origin).
 */
enum class CoordinateType {
    /**
     * Barycentric coordinates (origin at solar system barycenter).
     */
    BARYCENTRIC,
    
    /**
     * Geocentric coordinates (origin at Earth's center).
     */
    GEOCENTRIC,
    
    /**
     * Heliocentric coordinates (origin at Sun's center).
     */
    HELIOCENTRIC,
    
    /**
     * Topocentric coordinates (origin at observer's location).
     */
    TOPOCENTRIC
}

/**
 * Complete position and velocity information for a celestial body.
 *
 * @property body The celestial body
 * @property julianDay Time of the position
 * @property position Position vector in Cartesian coordinates
 * @property velocity Velocity vector in Cartesian coordinates (null if not calculated)
 * @property referenceFrame Coordinate reference frame used
 * @property coordinateType Origin of the coordinate system
 */
data class BodyPosition(
    val body: Planet,
    val julianDay: JulianDay,
    val position: CartesianCoordinates,
    val velocity: CartesianCoordinates?,
    val referenceFrame: CoordinateReferenceFrame,
    val coordinateType: CoordinateType
) {
    /**
     * Distance from origin in kilometers.
     */
    fun distance(): Double = position.magnitude()
    
    /**
     * Speed in km/day (if velocity is available).
     */
    fun speed(): Double? = velocity?.magnitude()

    /**
     * Converts position to ecliptic coordinates.
     */
    fun toEcliptic(): EclipticCoordinates = position.toEcliptic()

    /**
     * Converts position to equatorial coordinates.
     */
    fun toEquatorial(): EquatorialCoordinates = position.toEquatorial()

    /**
     * Converts position to horizontal coordinates for an observer.
     *
     * @param observerLatitude Observer's latitude in degrees
     * @param localSiderealTime Local Sidereal Time in hours
     */
    fun toHorizontal(observerLatitude: Double, localSiderealTime: Double): HorizontalCoordinates =
        position.toHorizontal(observerLatitude, localSiderealTime)
}
