package ch.typedef.swekt.examples

import ch.typedef.swekt.houses.*
import ch.typedef.swekt.model.JulianDay

/**
 * Example demonstrating house calculations.
 *
 * Run with: ./gradlew run -PmainClass=ch.typedef.swekt.examples.HouseSystemExample
 */
fun main() {
    println("=".repeat(70))
    println("House System Calculations Example")
    println("=".repeat(70))
    println()

    val calculator = StandardHouseCalculator()

    // Test data: Birth at Greenwich, January 1, 2000, noon
    val birthTime = JulianDay.fromGregorian(2000, 1, 1, 12.0)
    val birthPlace = GeographicLocation.GREENWICH

    println("Birth Data:")
    println("  Time: 2000-01-01 12:00 UT (J2000)")
    println("  Place: Greenwich (51.48°N, 0.00°E)")
    println()

    // Test different house systems
    val systems = listOf(
        HouseSystem.PLACIDUS,
        HouseSystem.KOCH,
        HouseSystem.PORPHYRY,
        HouseSystem.EQUAL,
        HouseSystem.WHOLE_SIGN,
        HouseSystem.CAMPANUS,
        HouseSystem.REGIOMONTANUS
    )

    for (system in systems) {
        println("-".repeat(70))
        println("${system.displayName} Houses (code: ${system.code})")
        println("-".repeat(70))

        val houses = calculator.calculate(birthTime, birthPlace, system)

        // Print angles
        println("Angles:")
        println("  Ascendant (ASC): ${formatDegrees(houses.ascendant)}")
        println("  Medium Coeli (MC): ${formatDegrees(houses.mc)}")
        println("  Descendant (DSC): ${formatDegrees(houses.descendant)}")
        println("  Imum Coeli (IC): ${formatDegrees(houses.ic)}")
        println("  ARMC: ${formatDegrees(houses.armc)}")
        println()

        // Print house cusps
        println("House Cusps:")
        for (i in 1..12) {
            val cusp = houses.getCusp(i)
            val sign = getZodiacSign(cusp)
            val degInSign = cusp % 30.0
            println("  House %2d: %s  (%s %.2f°)".format(
                i,
                formatDegrees(cusp),
                sign,
                degInSign
            ))
        }
        println()

        // Print additional points
        println("Additional Points:")
        println("  Vertex: ${formatDegrees(houses.vertex)}")
        println("  Equatorial Ascendant: ${formatDegrees(houses.equatorialAscendant)}")
        println("  Co-Ascendant (Koch): ${formatDegrees(houses.coAscendantKoch)}")
        println("  Co-Ascendant (Munkasey): ${formatDegrees(houses.coAscendantMunkasey)}")
        println("  Polar Ascendant: ${formatDegrees(houses.polarAscendant)}")
        println()
    }

    // Compare different locations
    println("=".repeat(70))
    println("Same Time, Different Locations (Equal Houses)")
    println("=".repeat(70))
    println()

    val locations = mapOf(
        "Greenwich" to GeographicLocation.GREENWICH,
        "New York" to GeographicLocation.NEW_YORK,
        "Tokyo" to GeographicLocation.TOKYO,
        "Sydney" to GeographicLocation.SYDNEY
    )

    for ((name, location) in locations) {
        val houses = calculator.calculate(birthTime, location, HouseSystem.EQUAL)
        println("$name ($location):")
        println("  ASC: ${formatDegrees(houses.ascendant)} (${getZodiacSign(houses.ascendant)})")
        println("  MC:  ${formatDegrees(houses.mc)} (${getZodiacSign(houses.mc)})")
        println()
    }

    // Gauquelin Sectors example
    println("=".repeat(70))
    println("Gauquelin Sectors (36 sectors)")
    println("=".repeat(70))
    println()

    val gauquelin = calculator.calculate(birthTime, birthPlace, HouseSystem.GAUQUELIN)
    println("First 12 sectors (10° each):")
    for (i in 1..12) {
        val sector = gauquelin.getCusp(i)
        println("  Sector %2d: %s".format(i, formatDegrees(sector)))
    }
    println()
}

/**
 * Format degrees to DD°MM'SS" format.
 */
private fun formatDegrees(degrees: Double): String {
    val d = degrees.toInt()
    val m = ((degrees - d) * 60).toInt()
    val s = ((degrees - d - m / 60.0) * 3600)
    return "%3d°%02d'%05.2f\"".format(d, m, s)
}

/**
 * Get zodiac sign for given ecliptic longitude.
 */
private fun getZodiacSign(longitude: Double): String {
    val signs = arrayOf(
        "Aries", "Taurus", "Gemini", "Cancer",
        "Leo", "Virgo", "Libra", "Scorpio",
        "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )
    val signIndex = (longitude / 30.0).toInt() % 12
    return signs[signIndex]
}
