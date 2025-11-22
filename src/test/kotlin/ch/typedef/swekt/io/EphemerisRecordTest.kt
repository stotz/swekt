package ch.typedef.swekt.io

import ch.typedef.swekt.model.JulianDay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EphemerisRecordTest {

    @Test
    fun `should create record with valid data`() {
        val jd = JulianDay(2451545.0)
        val position = doubleArrayOf(1.0, 0.0, 0.0)
        val velocity = doubleArrayOf(0.0, 0.01, 0.0)
        
        val record = EphemerisRecord(jd, position, velocity)
        
        assertThat(record.julianDay).isEqualTo(jd)
        assertThat(record.position).containsExactly(1.0, 0.0, 0.0)
        assertThat(record.velocity).containsExactly(0.0, 0.01, 0.0)
    }

    @Test
    fun `should reject invalid position size`() {
        val jd = JulianDay(2451545.0)
        val invalidPosition = doubleArrayOf(1.0, 0.0) // Only 2 components
        val velocity = doubleArrayOf(0.0, 0.01, 0.0)
        
        assertThrows<IllegalArgumentException> {
            EphemerisRecord(jd, invalidPosition, velocity)
        }
    }

    @Test
    fun `should reject invalid velocity size`() {
        val jd = JulianDay(2451545.0)
        val position = doubleArrayOf(1.0, 0.0, 0.0)
        val invalidVelocity = doubleArrayOf(0.0, 0.01) // Only 2 components
        
        assertThrows<IllegalArgumentException> {
            EphemerisRecord(jd, position, invalidVelocity)
        }
    }

    @Test
    fun `should implement equals correctly`() {
        val jd = JulianDay(2451545.0)
        val pos = doubleArrayOf(1.0, 0.0, 0.0)
        val vel = doubleArrayOf(0.0, 0.01, 0.0)
        
        val record1 = EphemerisRecord(jd, pos.clone(), vel.clone())
        val record2 = EphemerisRecord(jd, pos.clone(), vel.clone())
        
        assertThat(record1).isEqualTo(record2)
    }

    @Test
    fun `should implement hashCode correctly`() {
        val jd = JulianDay(2451545.0)
        val pos = doubleArrayOf(1.0, 0.0, 0.0)
        val vel = doubleArrayOf(0.0, 0.01, 0.0)
        
        val record1 = EphemerisRecord(jd, pos.clone(), vel.clone())
        val record2 = EphemerisRecord(jd, pos.clone(), vel.clone())
        
        assertThat(record1.hashCode()).isEqualTo(record2.hashCode())
    }
}
