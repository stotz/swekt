# Java Interoperability Guide

swekt is designed to be fully usable from Java. All public APIs are annotated for optimal Java interop.

## Quick Start

```java
import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.model.Planet;
import ch.typedef.swekt.model.GregorianDate;

public class SwektExample {
    public static void main(String[] args) {
        // Create Julian Day from value
        JulianDay jd = new JulianDay(2451545.0);
        System.out.println("Julian Day: " + jd.getValue());
        
        // Create Julian Day from Gregorian date
        JulianDay jd2 = JulianDay.fromGregorian(2000, 1, 1, 12.0);
        
        // Access J2000 constant
        JulianDay j2000 = JulianDay.J2000;
        
        // Arithmetic operations
        JulianDay tomorrow = jd.plus(1.0);
        double diff = tomorrow.minus(jd);
        
        // Convert back to Gregorian
        GregorianDate gregorian = jd.toGregorian();
        System.out.println("Date: " + gregorian);
        
        // Work with planets
        Planet mars = Planet.MARS;
        System.out.println("Planet ID: " + mars.getId());
        System.out.println("Name: " + mars.getDisplayName());
        
        // Lookup by ID
        Planet planet = Planet.fromId(4);
        
        // Get planet lists
        List<Planet> mainPlanets = Planet.mainPlanets();
        List<Planet> classical = Planet.classicalPlanets();
    }
}
```

## Gradle Configuration

Add swekt to your `build.gradle`:

```gradle
dependencies {
    implementation 'ch.typedef:swekt:0.0.1'
}
```

Or in Kotlin DSL (`build.gradle.kts`):

```kotlin
dependencies {
    implementation("ch.typedef:swekt:0.0.1")
}
```

## Maven Configuration

```xml
<dependency>
    <groupId>ch.typedef</groupId>
    <artifactId>swekt</artifactId>
    <version>0.0.1</version>
</dependency>
```

## Key Java Interop Features

### 1. Static Methods

All companion object methods are accessible as static methods in Java:

```java
// Kotlin
JulianDay.fromGregorian(2000, 1, 1)

// Java
JulianDay.fromGregorian(2000, 1, 1);
```

### 2. Default Parameters

Methods with default parameters have overloaded versions in Java:

```java
// With all parameters
JulianDay jd = JulianDay.fromGregorian(2000, 1, 1, 12.0);

// With default hour (12.0)
JulianDay jd = JulianDay.fromGregorian(2000, 1, 1);
```

### 3. Regular Class (not Value Class)

JulianDay is a regular `data class` for Java compatibility:

```java
// Create instances
JulianDay jd = new JulianDay(2451545.0);

// Access properties
double value = jd.getValue();

// Use methods
JulianDay tomorrow = jd.plus(1.0);
```

**Note:** Originally a value class for performance, but changed to data class for Java interop.

### 4. Enum Support

Kotlin enums work seamlessly in Java:

```java
Planet mars = Planet.MARS;
int id = mars.getId();
String name = mars.getDisplayName();
boolean isClassical = mars.isClassical();
```

### 5. Data Class Properties

Data classes have standard getter methods:

```java
GregorianDate date = jd.toGregorian();
int year = date.getYear();
int month = date.getMonth();
int day = date.getDay();
double hour = date.getHour();
```

### 6. Constants

Constants are accessible as static fields:

```java
// Access J2000 constant directly
JulianDay j2000 = JulianDay.J2000;
```

## Best Practices

### 1. Use Static Imports

```java
import static ch.typedef.swekt.model.Planet.*;

Planet mars = MARS;  // Cleaner!
```

### 2. Handle Nullability

Kotlin's `?` types map to `@Nullable` in Java. Use proper null checks:

```java
Planet planet = Planet.fromId(999);
if (planet != null) {
    // Use planet
}
```

### 3. Prefer Immutability

swekt objects are immutable - embrace it in Java too:

```java
// Good - create new instances
JulianDay tomorrow = today.plus(1.0);

// Bad - don't try to mutate (won't compile anyway)
// today.setValue(123.0);  // No setter exists
```

## IDE Support

### IntelliJ IDEA
- Full code completion for Kotlin libraries
- Jump to source works seamlessly
- Documentation available (KDoc → Javadoc)

### Eclipse
- Requires Kotlin plugin
- Most features work

### VS Code
- With Java extensions works well
- Kotlin support via plugins

## Troubleshooting

### "Cannot find symbol: JulianDay"
- Ensure swekt is in dependencies
- Run `./gradlew build` to compile Kotlin code first

### "Method fromGregorian() not found"
- Check for `@JvmStatic` annotation in Kotlin source
- Clean and rebuild: `./gradlew clean build`

### "Field J2000 is not visible"
- Value class constants use `@JvmStatic` getter methods
- Use: `JulianDay.getJ2000()` (not direct field access)

## Examples

### Calculate Days Between Dates

```java
JulianDay date1 = JulianDay.fromGregorian(2000, 1, 1);
JulianDay date2 = JulianDay.fromGregorian(2000, 12, 31);

double daysBetween = date2.minus(date1);
System.out.println("Days: " + daysBetween);
```

### Work with Planet Lists

```java
List<Planet> classical = Planet.classicalPlanets();
for (Planet planet : classical) {
    System.out.println(planet.getDisplayName() + " (ID: " + planet.getId() + ")");
}
```

### Convert Date Formats

```java
JulianDay jd = JulianDay.fromGregorian(2000, 6, 15, 18.5);
GregorianDate gregorian = jd.toGregorian();
System.out.println("Formatted: " + gregorian.toString());
```

---

**swekt is designed for Java!** All core APIs are Java-friendly with proper annotations. 🎯
