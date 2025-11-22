package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay

/**
 * Single ephemeris record from binary file.
 *
 * Contains position and velocity data for one celestial body at one point in time.
 *
 * @property julianDay Time of the record
 * @property position Position vector [x, y, z] in AU
 * @property velocity Velocity vector [dx/dt, dy/dt, dz/dt] in AU/day
 */
data class EphemerisRecord(
    val julianDay: JulianDay,
    val position: DoubleArray,
    val velocity: DoubleArray
) {
    init {
        require(position.size == 3) { "Position must have 3 components, got ${position.size}" }
        require(velocity.size == 3) { "Velocity must have 3 components, got ${velocity.size}" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EphemerisRecord

        if (julianDay != other.julianDay) return false
        if (!position.contentEquals(other.position)) return false
        if (!velocity.contentEquals(other.velocity)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = julianDay.hashCode()
        result = 31 * result + position.contentHashCode()
        result = 31 * result + velocity.contentHashCode()
        return result
    }
}
