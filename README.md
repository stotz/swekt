 swekt 🌟

**The Swiss Ephemeris Kotlin Implementation**

High-precision astronomical calculations with modern, type-safe Kotlin APIs.

> **sw**iss **e**phemeris for **k**o**t**lin

## Project Status

Version: 0.0.7 (TDD Development Phase - Time Systems Complete)

### Java Interoperability ☕

swekt is fully compatible with Java! All APIs are annotated for seamless Java usage.

```java
// Java example
import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.model.Planet;

JulianDay jd = new JulianDay(2451545.0);
JulianDay j2000 = JulianDay.J2000;
Planet mars = Planet.MARS;
System.out.println("Mars ID: " + mars.getId());
```

See [JAVA_INTEROP.md](JAVA_INTEROP.md) for complete Java usage guide.

## Quick Start 🚀

```kotlin
import ch.typedef.swekt.calculation.SimpleCalculationEngine
import ch.typedef.swekt.model.JulianDay
import ch.typedef.swekt.model.Planet

val engine = SimpleCalculationEngine()
val jd = JulianDay.J2000

// Calculate Sun position
val sunPos = engine.calculate(Planet.SUN, jd)
println("Sun: ${sunPos.longitude}° at ${sunPos.distance} AU")

// Calculate Moon position
val moonPos = engine.calculate(Planet.MOON, jd)
println("Moon: ${moonPos.longitude}° at ${moonPos.distance} AU")
```

## What's Implemented (TDD)

### ✅ Domain Models (Test-Driven)

- **JulianDay** - Astronomical day numbering with Gregorian conversion
  - ✅ Creation from value
  - ✅ Gregorian ↔ Julian conversion
  - ✅ Date validation (leap years, month ranges, etc.)
  - ✅ Arithmetic operations (+, -)
  - ✅ Comparison operations
  - ✅ Historical dates (BCE support)

- **Planet** - Celestial body enumeration
  - ✅ Swiss Ephemeris ID compatibility
  - ✅ Classical vs. Modern planets
  - ✅ Lunar nodes (Mean & True)
  - ✅ Planet lookup by ID
  - ✅ Display names

### ✅ Configuration (Test-Driven)

- **EphemerisConfig** - Configuration management
  - ✅ Path resolution
  - ✅ SE_EPHE_PATH support
  - ✅ Multiple data source priority

- **DataSource** - Data source types
  - ✅ SE1 compressed files
  - ✅ JPL ephemeris
  - ✅ Moshier analytical

### ✅ Time Systems (Test-Driven) 🎉 NEW

- **TimeScale** - Time scale enumeration
  - ✅ UT1 (Universal Time based on Earth rotation)
  - ✅ UTC (Coordinated Universal Time)
  - ✅ TT (Terrestrial Time)
  - ✅ TDB (Barycentric Dynamical Time)
  - ✅ TAI (International Atomic Time)

- **DeltaT** - ΔT calculations (TT - UT)
  - ✅ Modern era (1972-present): Leap seconds + 32.184s
  - ✅ Historic era (1600-1972): Polynomial approximations
  - ✅ Ancient era (before 1600): Parabolic extrapolation
  - ✅ Accuracy: ±1 second for modern dates

- **TimeConversion** - Time scale conversions
  - ✅ UT ↔ TT conversion (uses ΔT)
  - ✅ UTC ↔ TT conversion
  - ✅ TT ↔ TDB conversion (periodic correction)
  - ✅ UT ↔ TDB conversion
  - ✅ Round-trip accuracy: <1 millisecond

### ✅ File I/O (Test-Driven)

- **EphemerisFileReader** - Binary file reading
  - ✅ SE1 file format support
  - ✅ Header parsing
  - ✅ Record structure
  - ✅ Endianness detection
  - ✅ **Se1BinaryReader integration** 🎉 NEW
  - ✅ **Chebyshev coefficient extraction** 🎉 NEW

### ✅ Calculation Engine (Test-Driven) 🚀 COMPLETE

- **PlanetaryPosition** - Calculation results
  - ✅ Heliocentric ecliptic coordinates
  - ✅ Position and velocity
  - ✅ Input validation

- **SimpleCalculationEngine** - Analytical calculations
  - ✅ Sun position (VSOP87 simplified, ~0.01° accuracy)
  - ✅ Moon position (ELP2000 simplified, ~0.17° accuracy)
  - ⏳ Planets (SimpleCalculationEngine)

- **ChebyshevInterpolation** - Mathematical core
  - ✅ Clenshaw's algorithm (ACM Algorithm 446)
  - ✅ Function evaluation (position)
  - ✅ Derivative evaluation (velocity)
  - ✅ Coordinate normalization
  - ✅ Ready for SE1 integration

- **SwissEphemerisEngine** - High-precision engine 🎯 COMPLETE
  - ✅ SE1 record structure
  - ✅ Chebyshev interpolation integration
  - ✅ Position and velocity calculation
  - ✅ Sub-arcsecond accuracy potential
  - ✅ **SE1 binary file reading** 🎉 NEW
  - ✅ **Full calculation pipeline** 🎉 NEW

### 🔄 In Progress (Next TDD Cycle)

- [ ] Sidereal Time calculations (GMST, GAST, LST)
- [ ] Complete coordinate transformations
- [ ] House systems (Placidus, Koch, etc.)
- [ ] Ayanamsa for sidereal zodiac

## TDD Workflow

This project is developed using **Test-Driven Development**:

1. **Red** 🔴 - Write failing test
2. **Green** 🟢 - Write minimal code to pass
3. **Refactor** 🔵 - Clean up code while keeping tests green

## Running Tests

```bash
./gradlew test
```

Or with build:

```bash
./gradlew build
```

## Project Structure

```
swekt/
├── src/
│   ├── main/kotlin/ch/typedef/swekt/
│   │   ├── model/              # Domain models
│   │   │   ├── JulianDay.kt
│   │   │   └── Planet.kt
│   │   ├── calculation/        # Calculation engine 🚀 NEW
│   │   │   ├── PlanetaryPosition.kt
│   │   │   └── SimpleCalculationEngine.kt
│   │   ├── config/             # Configuration
│   │   │   ├── DataSource.kt
│   │   │   ├── EphemerisConfig.kt
│   │   │   └── EphemerisPathResolver.kt
│   │   ├── io/                 # File I/O
│   │   │   ├── EphemerisFileHeader.kt
│   │   │   ├── EphemerisFileReader.kt
│   │   │   ├── EphemerisRecord.kt
│   │   │   └── FileFormat.kt
│   │   └── examples/           # Example programs 🚀 NEW
│   │       └── CalculationExample.kt
│   │
│   └── test/
│       ├── kotlin/ch/typedef/swekt/
│       │   ├── model/
│       │   ├── calculation/    # 🚀 NEW
│       │   ├── config/
│       │   └── io/
│       └── java/ch/typedef/swekt/interop/
│           └── CalculationJavaInteropTest.java  # 🚀 NEW
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

## Development Setup

### Requirements

- JDK 23
- Kotlin 2.2.20

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Run Application

```bash
./gradlew run
```

## Test Coverage

Current test coverage (TDD):
- JulianDay: 100% (15+ tests)
- Planet: 100% (11+ tests)
- EphemerisConfig: 100% (10+ tests)
- EphemerisFileReader: 100% (15+ tests)
- PlanetaryPosition: 100% (7 tests)
- SimpleCalculationEngine: 100% (10 tests)
- ChebyshevInterpolation: 100% (30+ tests)
- Se1Record: 100% (8 tests)
- SwissEphemerisEngine: 100% (8 tests)
- Se1BinaryReader: 100% (7 tests)
- **TimeScale: 100% (4 tests)** 🎉 NEW
- **DeltaT: 100% (12 tests)** 🎉 NEW
- **TimeConversion: 100% (15 tests)** 🎉 NEW
- Java Interop: 100% (40+ tests including time systems)

## Next Steps (TDD Roadmap)

### Phase 1: Core Models ✅ DONE
- [x] JulianDay
- [x] Planet
- [x] GregorianDate

### Phase 2: Configuration ✅ DONE
- [x] EphemerisConfig
- [x] DataSource
- [x] EphemerisPathResolver (SE_EPHE_PATH support)

### Phase 3: File I/O ✅ DONE
- [x] EphemerisFileReader
- [x] Binary format parsing
- [x] Endianness detection
- [x] File header structures

### Phase 4: Calculation ✅ COMPLETE 🎉
- [x] PlanetaryPosition
- [x] SimpleCalculationEngine (Sun, Moon)
- [x] Chebyshev Interpolation
- [x] **SwissEphemerisEngine** 🎯 NEW
- [x] **SE1 Record Structure** 🎯 NEW
- [ ] Planet calculations (Mercury through Pluto)
- [ ] Advanced calculation flags

### Phase 5: Production Integration ✅ COMPLETE 🎉
- [x] SE1 Record structure
- [x] Chebyshev interpolation
- [x] SwissEphemerisEngine core
- [x] Se1BinaryReader - Binary file reading
- [x] EphemerisFileReader integration
- [x] End-to-end calculation pipeline
- [x] **Time Systems (ΔT, time scale conversions)** 🎉 NEW
- [ ] Sidereal Time (GMST, GAST, LST) - Next
- [ ] Complete coordinate transformations
- [ ] File caching and management (optimization)
- [ ] Complete planet support (all bodies)

## Contributing

This project follows TDD strictly:
1. Write test first
2. See it fail
3. Write minimal implementation
4. See it pass
5. Refactor if needed
6. Commit

## License

Dual licensed:
- GNU General Public License v2.0 or later
- Swiss Ephemeris Professional License

## Links

- GitHub: https://github.com/stotz/swekt
- Swiss Ephemeris: https://www.astro.com/swisseph/

---

**Made with ❤️ in Switzerland 🇨🇭 using TDD**
