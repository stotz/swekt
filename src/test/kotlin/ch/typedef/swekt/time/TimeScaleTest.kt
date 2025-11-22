package ch.typedef.swekt.time

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("TimeScale Tests")
class TimeScaleTest {

    @Test
    @DisplayName("All time scales are defined")
    fun testAllTimeScales() {
        val scales = TimeScale.values()
        
        assertThat(scales).containsExactlyInAnyOrder(
            TimeScale.UT1,
            TimeScale.UTC,
            TimeScale.TT,
            TimeScale.TDB,
            TimeScale.TAI
        )
    }

    @Test
    @DisplayName("Default time scale is TT")
    fun testDefaultTimeScale() {
        val defaultScale = TimeScale.getDefault()
        
        assertThat(defaultScale).isEqualTo(TimeScale.TT)
    }

    @Test
    @DisplayName("Time scale enum can be compared")
    fun testTimeScaleComparison() {
        assertThat(TimeScale.TT).isEqualTo(TimeScale.TT)
        assertThat(TimeScale.UTC).isNotEqualTo(TimeScale.TT)
    }

    @Test
    @DisplayName("Time scale has meaningful names")
    fun testTimeScaleNames() {
        assertThat(TimeScale.UT1.name).isEqualTo("UT1")
        assertThat(TimeScale.UTC.name).isEqualTo("UTC")
        assertThat(TimeScale.TT.name).isEqualTo("TT")
        assertThat(TimeScale.TDB.name).isEqualTo("TDB")
        assertThat(TimeScale.TAI.name).isEqualTo("TAI")
    }
}
