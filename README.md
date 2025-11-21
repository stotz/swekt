 swekt 🌟

**The Swiss Ephemeris Kotlin Implementation**

High-precision astronomical calculations with modern, type-safe Kotlin APIs.

> **sw**iss **e**phemeris for **k**o**t**lin

## Project Status

Version: 0.0.2 (TDD Development Phase)

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

### 🔄 In Progress (Next TDD Cycle)

- [ ] PlanetaryPosition - Calculation results
- [ ] CalculationFlags - Configuration options
- [ ] EphemerisConfig - Path resolution
- [ ] EphemerisFileReader - Binary .se1 file reading

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
│   │   ├── model/           # Domain models
│   │   │   ├── JulianDay.kt
│   │   │   └── Planet.kt
│   │   ├── calculation/     # Calculation engine
│   │   ├── config/          # Configuration
│   │   └── io/              # File I/O
│   │
│   └── test/kotlin/ch/typedef/swekt/
│       └── model/
│           ├── JulianDayTest.kt
│           └── PlanetTest.kt
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
- JulianDay: 100% (15 tests)
- Planet: 100% (11 tests)

## Next Steps (TDD Roadmap)

### Phase 1: Core Models ✅ DONE
- [x] JulianDay
- [x] Planet
- [x] GregorianDate

### Phase 2: Configuration (Next)
- [ ] EphemerisConfig
- [ ] DataSource
- [ ] EphemerisPathResolver (SE_EPHE_PATH support)

### Phase 3: File I/O
- [ ] EphemerisFileReader
- [ ] Binary format parsing
- [ ] Endianness detection
- [ ] File caching

### Phase 4: Calculation
- [ ] PlanetaryPosition
- [ ] CalculationResult
- [ ] CalculationFlags
- [ ] Basic calculation engine

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
