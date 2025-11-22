package ch.typedef.swekt.examples

import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.time.DeltaT
import ch.typedef.swekt.time.TimeConversion
import ch.typedef.swekt.time.TimeScale

/**
 * Examples demonstrating time system conversions.
 *
 * Time systems in astronomy:
 * - UTC: Civil time (what clocks show)
 * - UT/UT1: Universal Time based on Earth's rotation
 * - TT: Terrestrial Time (uniform atomic time for Earth)
 * - TDB: Barycentric Dynamical Time (for solar system calculations)
 */
fun main() {
    println("=== Time Systems Example ===\n")
    
    // Example 1: Basic Delta T calculation
    println("Example 1: Delta T (ΔT = TT - UT)")
    println("-".repeat(50))
    
    val j2000 = JulianDay.J2000
    val deltaTDays = DeltaT.calculate(j2000)
    val deltaTSeconds = DeltaT.calculateSeconds(j2000.value)
    
    println("At J2000.0 (2000-01-01 12:00:00 TT):")
    println("  ΔT = ${"%.6f".format(deltaTDays)} days")
    println("  ΔT = ${"%.3f".format(deltaTSeconds)} seconds")
    println()
    
    // Example 2: Delta T over time
    println("Example 2: Delta T Evolution")
    println("-".repeat(50))
    
    val years = listOf(1900, 1950, 2000, 2020)
    years.forEach { year ->
        val jd = JulianDay.fromGregorian(year, 1, 1, 0.0)
        val dt = DeltaT.calculateSeconds(jd.value)
        println("Year $year: ΔT = ${"%.2f".format(dt)} seconds")
    }
    println()
    
    // Example 3: Time scale conversions
    println("Example 3: Time Scale Conversions")
    println("-".repeat(50))
    
    // User provides UTC time (what a clock shows)
    val utcTime = JulianDay.fromGregorian(2020, 6, 21, 12.0)
    println("Input: 2020-06-21 12:00:00 UTC")
    println("  JD(UTC) = ${"%.6f".format(utcTime.value)}")
    
    // Convert to TT (needed for most calculations)
    val ttTime = TimeConversion.utcToTT(utcTime)
    println("\nConverted to Terrestrial Time:")
    println("  JD(TT) = ${"%.6f".format(ttTime.value)}")
    println("  Difference: ${"%.3f".format((ttTime.value - utcTime.value) * 86400.0)} seconds")
    
    // Convert to TDB (needed for JPL ephemeris)
    val tdbTime = TimeConversion.ttToTDB(ttTime)
    println("\nConverted to Barycentric Dynamical Time:")
    println("  JD(TDB) = ${"%.6f".format(tdbTime.value)}")
    println("  TDB - TT: ${"%.3f".format((tdbTime.value - ttTime.value) * 86400000.0)} milliseconds")
    println()
    
    // Example 4: Round trip conversion
    println("Example 4: Round Trip Accuracy")
    println("-".repeat(50))
    
    val original = JulianDay.fromGregorian(2020, 1, 1, 0.0)
    println("Original UT: ${"%.10f".format(original.value)}")
    
    val tt1 = TimeConversion.utToTT(original)
    println("After UT→TT: ${"%.10f".format(tt1.value)}")
    
    val tdb = TimeConversion.ttToTDB(tt1)
    println("After TT→TDB: ${"%.10f".format(tdb.value)}")
    
    val tt2 = TimeConversion.tdbToTT(tdb)
    println("After TDB→TT: ${"%.10f".format(tt2.value)}")
    
    val final = TimeConversion.ttToUT(tt2)
    println("After TT→UT: ${"%.10f".format(final.value)}")
    
    val error = (final.value - original.value) * 86400.0 * 1000.0 // milliseconds
    println("Round trip error: ${"%.6f".format(error)} milliseconds")
    println()
    
    // Example 5: Historical dates
    println("Example 5: Historical Delta T")
    println("-".repeat(50))
    
    val historical = listOf(
        -499 to "500 BCE",
        0 to "1 BCE/1 CE",
        1000 to "1000 CE",
        1582 to "1582 CE (Gregorian reform)",
        1800 to "1800 CE",
        1900 to "1900 CE"
    )
    
    historical.forEach { (year, label) ->
        val jd = JulianDay.fromGregorian(year, 1, 1, 0.0)
        val dt = DeltaT.calculateSeconds(jd.value)
        val hours = dt / 3600.0
        println("$label: ΔT = ${"%.1f".format(dt)} s (${"%.2f".format(hours)} hours)")
    }
    println()
    
    // Example 6: Practical use case
    println("Example 6: Practical Use - Eclipse Calculation")
    println("-".repeat(50))
    
    // For eclipse calculations, we need precise time
    val eclipseUTC = JulianDay.fromGregorian(2024, 4, 8, 18.333333) // 18:20:00
    println("Eclipse time (UTC): 2024-04-08 18:20:00")
    println("  JD(UTC) = ${"%.6f".format(eclipseUTC.value)}")
    
    val eclipseTT = TimeConversion.utcToTT(eclipseUTC)
    println("\nFor calculations use TT:")
    println("  JD(TT) = ${"%.6f".format(eclipseTT.value)}")
    
    val deltaT = TimeConversion.getDeltaTSeconds(eclipseUTC)
    println("\nΔT correction: ${"%.2f".format(deltaT)} seconds")
    println("Without ΔT correction, eclipse time would be off by ${"%d".format(deltaT.toInt())} seconds!")
    println()
    
    // Example 7: Time scale summary
    println("Example 7: Time Scale Summary")
    println("-".repeat(50))
    
    val now = JulianDay.fromGregorian(2020, 7, 1, 12.0)
    
    println("Time scales for 2020-07-01 12:00:00:")
    println("  ${TimeScale.UTC}: User input (civil time)")
    println("  ${TimeScale.UT1}: Earth rotation (~0.9s diff from UTC)")
    println("  ${TimeScale.TAI}: Atomic time (UTC + leap seconds)")
    println("  ${TimeScale.TT}: Terrestrial Time (TAI + 32.184s)")
    println("  ${TimeScale.TDB}: Barycentric Time (TT + periodic correction)")
    println()
    println("Default time scale for calculations: ${TimeScale.getDefault()}")
}
