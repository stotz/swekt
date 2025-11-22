package ch.typedef.swekt.coordinates

import ch.typedef.swekt.model.CartesianCoordinates
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import kotlin.math.sqrt

class CoordinateTransformationsTest {

    private val tolerance = Offset.offset(0.0001)

    @Test
    fun `cartesian to ecliptic conversion`() {
        // Test point: x=1 AU in ecliptic plane
        val cartesian = CartesianCoordinates(149_597_870.7, 0.0, 0.0)
        val ecliptic = CoordinateTransformations.cartesianToEcliptic(cartesian)

        assertThat(ecliptic.longitude).isCloseTo(0.0, tolerance)
        assertThat(ecliptic.latitude).isCloseTo(0.0, tolerance)
        assertThat(ecliptic.distance).isCloseTo(149_597_870.7, Offset.offset(1.0))
    }

    @Test
    fun `ecliptic to cartesian round trip`() {
        val original = EclipticCoordinates(45.0, 10.0, 1.0)
        val cartesian = CoordinateTransformations.eclipticToCartesian(original)
        val back = CoordinateTransformations.cartesianToEcliptic(cartesian)

        assertThat(back.longitude).isCloseTo(original.longitude, tolerance)
        assertThat(back.latitude).isCloseTo(original.latitude, tolerance)
        assertThat(back.distance).isCloseTo(original.distance, tolerance)
    }

    @Test
    fun `cartesian to equatorial conversion`() {
        // Test point: x=1 AU pointing to vernal equinox
        val cartesian = CartesianCoordinates(149_597_870.7, 0.0, 0.0)
        val equatorial = CoordinateTransformations.cartesianToEquatorial(cartesian)

        assertThat(equatorial.rightAscension).isCloseTo(0.0, tolerance)
        assertThat(equatorial.declination).isCloseTo(0.0, tolerance)
        assertThat(equatorial.distance).isCloseTo(149_597_870.7, Offset.offset(1.0))
    }

    @Test
    fun `equatorial to cartesian round trip`() {
        val original = EquatorialCoordinates(6.0, 30.0, 1.0)  // 6h RA, +30° Dec
        val cartesian = CoordinateTransformations.equatorialToCartesian(original)
        val back = CoordinateTransformations.cartesianToEquatorial(cartesian)

        assertThat(back.rightAscension).isCloseTo(original.rightAscension, tolerance)
        assertThat(back.declination).isCloseTo(original.declination, tolerance)
        assertThat(back.distance).isCloseTo(original.distance, tolerance)
    }

    @Test
    fun `ecliptic to equatorial conversion at vernal equinox`() {
        // At vernal equinox: λ=0°, β=0° should give α=0h, δ=0°
        val ecliptic = EclipticCoordinates(0.0, 0.0, 1.0)
        val equatorial = CoordinateTransformations.eclipticToEquatorial(ecliptic)

        assertThat(equatorial.rightAscension).isCloseTo(0.0, tolerance)
        assertThat(equatorial.declination).isCloseTo(0.0, tolerance)
    }

    @Test
    fun `ecliptic to equatorial at summer solstice`() {
        // Summer solstice: λ=90°, β=0° should give δ = +ε (obliquity)
        val ecliptic = EclipticCoordinates(90.0, 0.0, 1.0)
        val equatorial = CoordinateTransformations.eclipticToEquatorial(ecliptic)

        assertThat(equatorial.declination)
            .isCloseTo(CoordinateTransformations.OBLIQUITY_J2000, Offset.offset(0.01))
    }

    @Test
    fun `ecliptic equatorial round trip`() {
        val original = EclipticCoordinates(120.0, 15.0, 2.5)
        val equatorial = CoordinateTransformations.eclipticToEquatorial(original)
        val back = CoordinateTransformations.equatorialToEcliptic(equatorial)

        assertThat(back.longitude).isCloseTo(original.longitude, tolerance)
        assertThat(back.latitude).isCloseTo(original.latitude, tolerance)
        assertThat(back.distance).isCloseTo(original.distance, tolerance)
    }

    @Test
    fun `equatorial to horizontal at zenith`() {
        // Object at observer's zenith: Dec = Lat, HA = 0
        val observerLat = 52.0  // degrees north
        val equatorial = EquatorialCoordinates(12.0, observerLat, 1.0)  // 12h RA
        val lst = 12.0  // LST = RA means HA = 0 (object on meridian)

        val horizontal = CoordinateTransformations.equatorialToHorizontal(
            equatorial, observerLat, lst
        )

        assertThat(horizontal.altitude).isCloseTo(90.0, Offset.offset(0.1))  // At zenith
    }

    @Test
    fun `equatorial to horizontal at horizon`() {
        // Object rising: altitude should be ~0°
        val observerLat = 52.0
        val equatorial = EquatorialCoordinates(0.0, 0.0, 1.0)  // On celestial equator
        val lst = 6.0  // Hour angle = 90°

        val horizontal = CoordinateTransformations.equatorialToHorizontal(
            equatorial, observerLat, lst
        )

        // At HA=90°, object is on horizon for equatorial position
        assertThat(horizontal.altitude).isCloseTo(0.0, Offset.offset(5.0))
    }

    @Test
    fun `zodiac sign calculation`() {
        assertThat(EclipticCoordinates(0.0, 0.0, 1.0).zodiacSign())
            .isEqualTo(ZodiacSign.ARIES)
        
        assertThat(EclipticCoordinates(30.0, 0.0, 1.0).zodiacSign())
            .isEqualTo(ZodiacSign.TAURUS)
        
        assertThat(EclipticCoordinates(90.0, 0.0, 1.0).zodiacSign())
            .isEqualTo(ZodiacSign.CANCER)
        
        assertThat(EclipticCoordinates(180.0, 0.0, 1.0).zodiacSign())
            .isEqualTo(ZodiacSign.LIBRA)
        
        assertThat(EclipticCoordinates(270.0, 0.0, 1.0).zodiacSign())
            .isEqualTo(ZodiacSign.CAPRICORN)
    }

    @Test
    fun `position in sign calculation`() {
        val coords = EclipticCoordinates(45.5, 0.0, 1.0)
        
        assertThat(coords.zodiacSign()).isEqualTo(ZodiacSign.TAURUS)
        assertThat(coords.positionInSign()).isCloseTo(15.5, tolerance)
    }

    @Test
    fun `astrological string formatting`() {
        val coords = EclipticCoordinates(45.5, 0.0, 1.0)
        val str = coords.toAstrologicalString()
        
        assertThat(str).contains("♉")  // Taurus symbol
        assertThat(str).contains("15°")
        assertThat(str).contains("Taurus")
    }

    @Test
    fun `equatorial HMS DMS formatting`() {
        val coords = EquatorialCoordinates(12.5, 45.5, 1.0)
        
        assertThat(coords.rightAscensionHMS()).matches("12h \\d{2}m \\d{2}s")
        assertThat(coords.declinationDMS()).matches("\\+45° \\d{2}' \\d{2}\"")
    }

    @Test
    fun `horizontal cardinal direction`() {
        assertThat(HorizontalCoordinates(0.0, 10.0, 1.0).cardinalDirection())
            .isEqualTo("N")
        
        assertThat(HorizontalCoordinates(90.0, 10.0, 1.0).cardinalDirection())
            .isEqualTo("E")
        
        assertThat(HorizontalCoordinates(180.0, 10.0, 1.0).cardinalDirection())
            .isEqualTo("S")
        
        assertThat(HorizontalCoordinates(270.0, 10.0, 1.0).cardinalDirection())
            .isEqualTo("W")
    }

    @Test
    fun `horizontal visibility check`() {
        val above = HorizontalCoordinates(45.0, 30.0, 1.0)
        val below = HorizontalCoordinates(45.0, -10.0, 1.0)
        
        assertThat(above.isAboveHorizon()).isTrue()
        assertThat(below.isAboveHorizon()).isFalse()
    }

    @Test
    fun `horizontal air mass calculation`() {
        val zenith = HorizontalCoordinates(0.0, 90.0, 1.0)
        val horizon = HorizontalCoordinates(0.0, 0.0, 1.0)
        val mid = HorizontalCoordinates(0.0, 45.0, 1.0)
        
        assertThat(zenith.airMass()).isCloseTo(1.0, Offset.offset(0.1))
        assertThat(horizon.airMass()).isPositive()
        assertThat(mid.airMass()).isGreaterThan(1.0).isLessThan(2.0)
    }

    @Test
    fun `coordinate normalization`() {
        // Test longitude wrapping
        val ecl1 = EclipticCoordinates.of(370.0, 10.0, 1.0)
        assertThat(ecl1.longitude).isCloseTo(10.0, tolerance)
        
        val ecl2 = EclipticCoordinates.of(-10.0, 10.0, 1.0)
        assertThat(ecl2.longitude).isCloseTo(350.0, tolerance)
        
        // Test RA wrapping
        val eq1 = EquatorialCoordinates.of(25.0, 10.0, 1.0)
        assertThat(eq1.rightAscension).isCloseTo(1.0, tolerance)
        
        val eq2 = EquatorialCoordinates.of(-1.0, 10.0, 1.0)
        assertThat(eq2.rightAscension).isCloseTo(23.0, tolerance)
    }
}
