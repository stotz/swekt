package ch.typedef.swekt.engine

import ch.typedef.swekt.io.JplEphemerisReader
import ch.typedef.swekt.math.ChebyshevInterpolation
import ch.typedef.swekt.model.*
import java.nio.file.Path
import kotlin.math.sqrt

/**
 * Calculation engine using JPL ephemeris files (DE440, DE441, etc.).
 *
 * This engine provides the highest precision planetary positions by reading
 * NASA JPL ephemeris files directly and using Chebyshev polynomial interpolation.
 *
 * Advantages:
 * - Highest accuracy (sub-meter for inner planets)
 * - NASA public domain (no licensing issues)
 * - Industry standard format
 *
 * @property ephemerisPath Path to JPL ephemeris file (e.g., de441.eph)
 */
class JplCalculationEngine(
    ephemerisPath: Path
) : PlanetCalculationEngine {

    private val reader = JplEphemerisReader(ephemerisPath)
    private val header = reader.readHeader()
    
    // Cache for last read record to avoid repeated file I/O
    private var cachedRecordNum: Int = -1
    private var cachedRecord: DoubleArray? = null

    init {
        println("JPL Ephemeris loaded: DE${header.deNumber}")
        println("Time span: ${header.startJD} - ${header.endJD} JD")
        println("Interval: ${header.intervalDays} days")
    }

    override fun calculatePosition(
        planet: Planet,
        julianDay: JulianDay,
        calculateVelocity: Boolean
    ): BodyPosition {
        // Map our Planet enum to JPL body
        val jplBody = when (planet) {
            Planet.SUN -> JplEphemerisReader.JplBody.SUN
            Planet.MOON -> JplEphemerisReader.JplBody.MOON
            Planet.MERCURY -> JplEphemerisReader.JplBody.MERCURY
            Planet.VENUS -> JplEphemerisReader.JplBody.VENUS
            Planet.EARTH -> JplEphemerisReader.JplBody.EARTH
            Planet.MARS -> JplEphemerisReader.JplBody.MARS
            Planet.JUPITER -> JplEphemerisReader.JplBody.JUPITER
            Planet.SATURN -> JplEphemerisReader.JplBody.SATURN
            Planet.URANUS -> JplEphemerisReader.JplBody.URANUS
            Planet.NEPTUNE -> JplEphemerisReader.JplBody.NEPTUNE
            Planet.PLUTO -> JplEphemerisReader.JplBody.PLUTO
            else -> throw IllegalArgumentException("Planet $planet not supported by JPL ephemeris")
        }

        // Find which record contains this time
        val recordNum = reader.findRecordNumber(julianDay, header)
        
        // Read record (use cache if possible)
        val record = if (recordNum == cachedRecordNum && cachedRecord != null) {
            cachedRecord!!
        } else {
            val newRecord = reader.readRecord(recordNum, header)
            cachedRecordNum = recordNum
            cachedRecord = newRecord
            newRecord
        }

        // Extract Chebyshev coefficients for this body
        val chebyData = reader.extractCoefficients(record, jplBody, julianDay, header)

        // Normalize time to [-1, 1] for Chebyshev interpolation
        val t = normalizeTime(
            julianDay.value,
            chebyData.startJD,
            chebyData.endJD
        )

        // Interpolate position
        val x = ChebyshevInterpolation.evaluate(t, chebyData.coefficientsX)
        val y = ChebyshevInterpolation.evaluate(t, chebyData.coefficientsY)
        val z = ChebyshevInterpolation.evaluate(t, chebyData.coefficientsZ)

        // JPL stores positions in kilometers (barycentric)
        val position = CartesianCoordinates(x, y, z)

        // Calculate velocity if requested
        val velocity = if (calculateVelocity) {
            calculateVelocityVector(chebyData, t)
        } else {
            null
        }

        return BodyPosition(
            body = planet,
            julianDay = julianDay,
            position = position,
            velocity = velocity,
            referenceFrame = CoordinateReferenceFrame.ICRF,
            coordinateType = CoordinateType.BARYCENTRIC
        )
    }

    /**
     * Calculates velocity by taking the time derivative of the Chebyshev polynomials.
     *
     * The velocity in km/day is computed using the chain rule:
     * dR/dt = dR/dT * dT/dt
     * where T is the normalized time in [-1, 1]
     */
    private fun calculateVelocityVector(
        chebyData: JplEphemerisReader.ChebyshevData,
        normalizedTime: Double
    ): CartesianCoordinates {
        // Time scaling factor: dT/dt = 2 / (endJD - startJD)
        val timeScale = 2.0 / (chebyData.endJD - chebyData.startJD)

        // Evaluate derivative of Chebyshev polynomials
        val vx = ChebyshevInterpolation.evaluateDerivative(
            normalizedTime,
            chebyData.coefficientsX
        ) * timeScale

        val vy = ChebyshevInterpolation.evaluateDerivative(
            normalizedTime,
            chebyData.coefficientsY
        ) * timeScale

        val vz = ChebyshevInterpolation.evaluateDerivative(
            normalizedTime,
            chebyData.coefficientsZ
        ) * timeScale

        // Velocity in km/day
        return CartesianCoordinates(vx, vy, vz)
    }

    /**
     * Normalizes Julian Day to the range [-1, 1] for Chebyshev interpolation.
     */
    private fun normalizeTime(jd: Double, startJD: Double, endJD: Double): Double {
        return 2.0 * (jd - startJD) / (endJD - startJD) - 1.0
    }

    /**
     * Checks if a given Julian Day is within the ephemeris coverage.
     */
    fun isInRange(julianDay: JulianDay): Boolean {
        return julianDay.value >= header.startJD && julianDay.value <= header.endJD
    }

    /**
     * Returns information about the loaded ephemeris.
     */
    fun getEphemerisInfo(): String {
        return buildString {
            appendLine("JPL Planetary Ephemeris DE${header.deNumber}")
            appendLine("Time Coverage: ${header.startJD} - ${header.endJD} JD")
            appendLine("Interval: ${header.intervalDays} days")
            appendLine("Astronomical Unit: ${header.astronomicalUnit} m")
            appendLine("Earth/Moon Ratio: ${header.earthMoonRatio}")
            appendLine("Reference Frame: ICRF")
        }
    }

    override fun getSupportedPlanets(): Set<Planet> {
        return setOf(
            Planet.SUN,
            Planet.MOON,
            Planet.MERCURY,
            Planet.VENUS,
            Planet.EARTH,
            Planet.MARS,
            Planet.JUPITER,
            Planet.SATURN,
            Planet.URANUS,
            Planet.NEPTUNE,
            Planet.PLUTO
        )
    }

    override fun getValidTimeRange(): ClosedRange<JulianDay> {
        return JulianDay(header.startJD)..JulianDay(header.endJD)
    }

    /**
     * Calculates geocentric position (Earth-centered) instead of barycentric.
     *
     * For most astronomical applications, geocentric positions are needed.
     * This is calculated as: planet_position - earth_position
     */
    fun calculateGeocentricPosition(
        planet: Planet,
        julianDay: JulianDay,
        calculateVelocity: Boolean = false
    ): BodyPosition {
        if (planet == Planet.EARTH) {
            // Earth's geocentric position is (0,0,0)
            return BodyPosition(
                body = planet,
                julianDay = julianDay,
                position = CartesianCoordinates(0.0, 0.0, 0.0),
                velocity = if (calculateVelocity) CartesianCoordinates(0.0, 0.0, 0.0) else null,
                referenceFrame = CoordinateReferenceFrame.ICRF,
                coordinateType = CoordinateType.GEOCENTRIC
            )
        }

        // Get barycentric positions
        val planetPos = calculatePosition(planet, julianDay, calculateVelocity)
        val earthPos = calculatePosition(Planet.EARTH, julianDay, calculateVelocity)

        // Calculate geocentric position
        val geocentricPos = CartesianCoordinates(
            x = planetPos.position.x - earthPos.position.x,
            y = planetPos.position.y - earthPos.position.y,
            z = planetPos.position.z - earthPos.position.z
        )

        val geocentricVel = if (calculateVelocity && planetPos.velocity != null && earthPos.velocity != null) {
            CartesianCoordinates(
                x = planetPos.velocity.x - earthPos.velocity.x,
                y = planetPos.velocity.y - earthPos.velocity.y,
                z = planetPos.velocity.z - earthPos.velocity.z
            )
        } else {
            null
        }

        return BodyPosition(
            body = planet,
            julianDay = julianDay,
            position = geocentricPos,
            velocity = geocentricVel,
            referenceFrame = CoordinateReferenceFrame.ICRF,
            coordinateType = CoordinateType.GEOCENTRIC
        )
    }
}
