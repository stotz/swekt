package ch.typedef.swekt.engine

import ch.typedef.swekt.model.BodyPosition
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Interface for planetary calculation engines.
 *
 * Different implementations can provide planetary positions:
 * - JPL ephemeris files (highest precision)
 * - Swiss Ephemeris files
 * - Analytical algorithms (VSOP87, etc.)
 */
interface PlanetCalculationEngine {
    
    /**
     * Calculates the position of a planet at a given time.
     *
     * @param planet The celestial body to calculate
     * @param julianDay Time for the calculation
     * @param calculateVelocity Whether to calculate velocity (optional, may be slower)
     * @return Complete position information including coordinates and metadata
     */
    fun calculatePosition(
        planet: Planet,
        julianDay: JulianDay,
        calculateVelocity: Boolean = false
    ): BodyPosition
    
    /**
     * Returns the set of planets supported by this engine.
     */
    fun getSupportedPlanets(): Set<Planet>
    
    /**
     * Returns the valid time range for this engine.
     */
    fun getValidTimeRange(): ClosedRange<JulianDay>
}
