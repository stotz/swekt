# Swiss Ephemeris File Format Documentation

## Overview

Swiss Ephemeris (SE) provides high-precision astronomical data through compressed binary files. This document describes all file types, their contents, time coverage, and accuracy.

## File Naming Convention

### Format: `se[type][century].se1`

- **type**: pl (planets), mo (moon), as (asteroids)
- **century**: Two-digit century number (00 = 0-99 CE, 18 = 1800-1899 CE)
- **extension**: .se1 (compressed Swiss Ephemeris)

### Examples:
- `sepl_18.se1` - Planets for 1800-1899 CE
- `semo_18.se1` - Moon for 1800-1899 CE
- `seas_18.se1` - Main asteroids for 1800-1899 CE

## File Types

### 1. Planet Files (sepl_*.se1)

**Contains:** Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn, Uranus, Neptune, Pluto

**Naming:** `sepl_[century].se1`

**Time Coverage:** Each file covers 100 years (1 century)

**Available Centuries:**
```
sepl_00.se1    0 CE - 99 CE
sepl_06.se1    600 CE - 699 CE
sepl_12.se1    1200 CE - 1299 CE
sepl_18.se1    1800 CE - 1899 CE     ⭐ Most commonly used
sepl_24.se1    2400 CE - 2499 CE
...
sepl_162.se1   16200 CE - 16299 CE
```

**Accuracy:**
- Positions: ±0.001 arcseconds
- Based on JPL DE431/DE441 ephemeris
- Suitable for professional astronomy and astrology

**File Size:** ~2-3 MB per century

### 2. Moon Files (semo_*.se1)

**Contains:** Moon positions with high time resolution

**Naming:** `semo_[century].se1`

**Time Coverage:** Same as planet files (100 years per file)

**Accuracy:**
- Positions: ±0.001 arcseconds
- Higher time resolution than planet files
- Includes lunar libration data

**File Size:** ~3-5 MB per century

### 3. Main Asteroid Files (seas_*.se1)

**Contains:** Main asteroids (typically Ceres, Pallas, Juno, Vesta, Chiron, Pholus)

**Naming:** `seas_[century].se1`

**Time Coverage:** Same as planet files (100 years per file)

**Asteroids Included:**
- 1 Ceres
- 2 Pallas
- 3 Juno
- 4 Vesta
- 5 Astraea
- 6 Hebe
- 7 Iris
- 8 Flora
- 9 Metis
- 10 Hygiea
- And additional main belt asteroids

**Accuracy:** ±0.1 arcseconds

**File Size:** ~1-2 MB per century

### 4. Moshier Files (seplm*.se1, semom*.se1, seasm*.se1)

**Contains:** Analytical ephemeris (Moshier algorithm)

**Purpose:** Fallback when compressed files unavailable

**Accuracy:** Lower than standard .se1 files but sufficient for most applications

**Advantages:**
- Smaller file size
- Built-in algorithms available (no files needed for basic calculations)

**Naming Pattern:**
- `seplm_[century].se1` - Planets (Moshier)
- `semom_[century].se1` - Moon (Moshier)
- `seasm_[century].se1` - Asteroids (Moshier)

### 5. Saturn Moon Files (sat/sepm*.se1)

**Location:** `sat/` subdirectory

**Contains:** Satellites of Saturn (Mimas, Enceladus, Tethys, Dione, Rhea, Titan, Hyperion, Iapetus, Phoebe)

**Naming:** `sepm[year].se1`

**Example:** `sepm9401.se1` - Saturn moons for 1994-1995

**Time Coverage:** Typically 1-2 years per file

**File Size:** ~100-500 KB per file

## JPL Ephemeris Files

### DE200.eph

**Size:** 41 MB  
**Time Coverage:** 1600 CE - 2170 CE  
**Accuracy:** ~1 km for planets  
**Status:** Older, superseded by DE406+  
**Use Case:** Historical calculations  

### DE406e.eph

**Size:** 190 MB  
**Time Coverage:** -3000 BCE - 3000 CE  
**Accuracy:** ~0.001 km for inner planets  
**Status:** Widely used standard  
**Use Case:** General purpose, historical to near-future  

### DE431.eph

**Size:** 2.6 GB  
**Time Coverage:** -13000 BCE - 17000 CE  
**Accuracy:** Best available for Moon  
**Status:** Current NASA/JPL standard  
**Use Case:** Professional astronomy, lunar calculations  

### DE440.eph

**Size:** 2.6 GB  
**Time Coverage:** -13000 BCE - 17000 CE  
**Accuracy:** Improved planetary accuracy  
**Status:** Newer than DE431  
**Use Case:** Modern professional work  

### DE441.eph ⭐

**Size:** 2.6 GB  
**Time Coverage:** -13000 BCE - 17000 CE  
**Accuracy:** Latest and most accurate  
**Status:** Current state-of-the-art (2021+)  
**Use Case:** Highest precision requirements  

**MD5 Checksums:**
```
1ef6191b614b2b854adae8675b1b981f  de200.eph
1ef768440cc1617b6c8ad27a9a788135  de406e.eph
fad0f432ae18c330f9e14915fbf8960a  de431.eph
a7b2a5b8b2ebed52ea4da2304958053b  de441.eph
```

## Additional Files

### sefstars.txt

**Contains:** Fixed star catalog

**Format:** Text file

**Content:**
- Star names
- Positions (RA/Dec)
- Magnitudes
- Proper motions

**Stars Included:** ~300+ brightest and astrologically significant stars

**Example Entries:**
```
Aldebaran (Alpha Tauri)
Regulus (Alpha Leonis)
Spica (Alpha Virginis)
Antares (Alpha Scorpii)
```

### seasnam.txt

**Contains:** Asteroid names and numbers

**Format:** Text file

**Purpose:** Mapping between asteroid numbers and names

## File Organization

### Recommended Directory Structure

```
C:\data\swisseph\
├── sepl_18.se1          # Core planet file (1800-1899)
├── semo_18.se1          # Core moon file (1800-1899)
├── seas_18.se1          # Core asteroid file (1800-1899)
├── sepl_00.se1          # Additional centuries as needed
├── sepl_06.se1
├── sepl_12.se1
├── sepl_24.se1
├── ...
├── de441.eph            # JPL file (optional, highest accuracy)
├── sefstars.txt         # Fixed stars (optional)
├── seasnam.txt          # Asteroid names (optional)
└── sat\                 # Saturn moons (optional)
    ├── sepm9401.se1
    ├── sepm9501.se1
    └── ...
```

## Time Coverage Matrix

| Century | Years       | sepl | semo | seas |
|---------|-------------|------|------|------|
| 00      | 0-99        | ✓    | ✓    | ✓    |
| 06      | 600-699     | ✓    | ✓    | ✓    |
| 12      | 1200-1299   | ✓    | ✓    | ✓    |
| 18      | 1800-1899   | ✓    | ✓    | ✓    |
| 24      | 2400-2499   | ✓    | ✓    | ✓    |
| 30      | 3000-3099   | ✓    | ✓    | ✓    |
| ...     | ...         | ...  | ...  | ...  |
| 162     | 16200-16299 | ✓    | ✓    | ✓    |

**Total Coverage:** 0 CE to 16299 CE (over 16,000 years!)

## Accuracy Comparison

| Source          | Planets       | Moon          | Time Range              |
|-----------------|---------------|---------------|-------------------------|
| Moshier         | ±1 arcsec     | ±2 arcsec     | -3000 to +3000 CE       |
| Swiss Ephe .se1 | ±0.001 arcsec | ±0.001 arcsec | 0 to 16299 CE           |
| JPL DE406       | ±0.001 km     | ±0.001 km     | -3000 to +3000 CE       |
| JPL DE431       | ±0.0001 km    | ±0.00001 km   | -13000 to +17000 CE     |
| JPL DE441 ⭐     | ±0.0001 km    | ±0.00001 km   | -13000 to +17000 CE     |

## Choosing the Right Files

### Minimal Installation (Most Common)

For dates 1800-2100:
```
sepl_18.se1   (1800-1899)
sepl_24.se1   (2400-2499 - covers 2000-2099)
semo_18.se1
semo_24.se1
seas_18.se1
seas_24.se1
```
**Total:** ~20 MB

### Extended Historical (0-3000 CE)

All century files from 00 to 30:
```
sepl_00.se1, sepl_06.se1, ..., sepl_30.se1
semo_00.se1, semo_06.se1, ..., semo_30.se1
seas_00.se1, seas_06.se1, ..., seas_30.se1
```
**Total:** ~300 MB

### Maximum Precision

Swiss Ephemeris files + JPL DE441:
```
All .se1 files + de441.eph
```
**Total:** ~3 GB

### Embedded Applications

Moshier only (no files needed):
- Built into Swiss Ephemeris library
- Sufficient for most astrology applications
- Dates: -3000 to +3000 CE
- No disk space required

## File Format Details

### Binary Format

All .se1 and .eph files are binary formats:
- **NOT** human-readable
- Compressed for efficiency
- Platform-independent (same file works on Windows/Linux/Mac)
- Little-endian byte order

### Accessing Data

Files are accessed through Swiss Ephemeris API:
```kotlin
// swekt will handle file reading
val ephemeris = SwissEphemeris(config)
val position = ephemeris.calculatePlanet(Planet.MARS, julianDay)
```

## Performance Considerations

### File Access Speed

| File Type | Read Speed | Recommended |
|-----------|------------|-------------|
| .se1      | Fast       | ✓ Best      |
| .eph      | Medium     | High precision |
| Moshier   | Fastest    | No files    |

### Memory Usage

- .se1 files: Portions loaded on demand (~1-5 MB RAM)
- .eph files: Larger memory footprint (~10-50 MB RAM)
- Moshier: Minimal (~100 KB RAM)

### Disk Space

- **Minimal:** 20 MB (core centuries)
- **Standard:** 300 MB (0-3000 CE)
- **Complete:** 3+ GB (all files + JPL)

## Download Sources

### Swiss Ephemeris Files (.se1)

**GitHub:**
https://github.com/aloistr/swisseph/tree/master/ephe

**Dropbox (Alois Treindl):**
https://www.dropbox.com/scl/fo/y3naz62gy6f6qfrhquu7u/h?rlkey=ejltdhb262zglm7eo6yfj2940&dl=0

### JPL Files (.eph)

**NASA JPL:**
https://ssd.jpl.nasa.gov/ftp/eph/planets/Linux/

**Dropbox (Alois Treindl):**
https://www.dropbox.com/scl/fo/y3naz62gy6f6qfrhquu7u/h?rlkey=ejltdhb262zglm7eo6yfj2940&dl=0

**Mirror (Phillip McCabe):**
https://ephe.scryr.io/jpl/

## License

Swiss Ephemeris files are:
- **Free** for non-commercial use (AGPL)
- **Paid license** required for commercial/closed-source applications

See: https://www.astro.com/swisseph/

## References

- Official Documentation: https://www.astro.com/swisseph
- GitHub Repository: https://github.com/aloistr/swisseph
- Mailing List: https://groups.io/g/swisseph

---

**Last Updated:** November 2024  
**Swiss Ephemeris Version:** 2.10+  
**Maintained by:** Dieter Koch, Alois Treindl (Astrodienst AG)
