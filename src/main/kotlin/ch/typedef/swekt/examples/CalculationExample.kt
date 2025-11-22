package ch.typedef.swekt.examples

import ch.typedef.swekt.calculation.SimpleCalculationEngine
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

/**
 * Example usage of swekt calculation engine.
 *
 * Demonstrates how to calculate Sun and Moon positions.
 */
fun main() {
    println("=== swekt Calculation Engine Demo ===\n")

    val engine = SimpleCalculationEngine()

    // Calculate Sun position at J2000.0
    println("Sun position at J2000.0:")
    val j2000 = JulianDay.J2000
    val sunPos = engine.calculate(Planet.SUN, j2000)
    println(sunPos)
    println("  Speed: ${String.format("%.4f", sunPos.longitudeSpeed)} degrees/day\n")

    // Calculate Moon position
    println("Moon position at J2000.0:")
    val moonPos = engine.calculate(Planet.MOON, j2000)
    println(moonPos)
    println("  Speed: ${String.format("%.4f", moonPos.longitudeSpeed)} degrees/day\n")

    // Calculate Sun through the year 2024
    println("Sun positions during 2024:")
    val year = 2024
    for (month in listOf(1, 3, 6, 9, 12)) {
        val jd = JulianDay.fromGregorian(year, month, 1, 12.0)
        val pos = engine.calculate(Planet.SUN, jd)
        val date = "$year-${String.format("%02d", month)}-01"
        val lon = String.format("%.2f", pos.longitude)
        println("  $date: longitude $lon°")
    }

    // Calculate Moon movement over a week
    println("\nMoon movement over 7 days:")
    val startDate = JulianDay.fromGregorian(2024, 11, 22, 0.0)
    for (day in 0..6) {
        val jd = startDate.plus(day.toDouble())
        val pos = engine.calculate(Planet.MOON, jd)
        val gregorian = jd.toGregorian()
        val lon = String.format("%.2f", pos.longitude)
        println("  Day $day (${gregorian.year}-${String.format("%02d", gregorian.month)}-${String.format("%02d", gregorian.day)}): $lon°")
    }

    println("\n=== Done ===")
}
