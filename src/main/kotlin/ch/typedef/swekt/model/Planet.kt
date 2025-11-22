package ch.typedef.swekt.model

/**
 * Celestial bodies supported by swekt.
 *
 * Planet IDs match the Swiss Ephemeris conventions for compatibility.
 *
 * @property id Swiss Ephemeris planet ID
 * @property displayName Human-readable name
 * @property isClassical True if this is a classical planet (visible to naked eye)
 * @property isModern True if this is a modern planet (requires telescope)
 * @property isNode True if this is a lunar node
 */
enum class Planet(
    val id: Int,
    val displayName: String,
    val isClassical: Boolean = false,
    val isModern: Boolean = false,
    val isNode: Boolean = false
) {
    SUN(0, "Sun", isClassical = true),
    MOON(1, "Moon", isClassical = true),
    MERCURY(2, "Mercury", isClassical = true),
    VENUS(3, "Venus", isClassical = true),
    EARTH(13, "Earth"),  // Special case - Earth itself
    MARS(4, "Mars", isClassical = true),
    JUPITER(5, "Jupiter", isClassical = true),
    SATURN(6, "Saturn", isClassical = true),
    URANUS(7, "Uranus", isModern = true),
    NEPTUNE(8, "Neptune", isModern = true),
    PLUTO(9, "Pluto", isModern = true),
    MEAN_NODE(10, "mean Node", isNode = true),
    TRUE_NODE(11, "true Node", isNode = true);

    companion object {
        /**
         * Finds a planet by its Swiss Ephemeris ID.
         *
         * @param id The planet ID
         * @return The planet, or null if not found
         */
        @JvmStatic
        fun fromId(id: Int): Planet? = entries.firstOrNull { it.id == id }

        /**
         * Returns all main planets (Sun through Pluto).
         */
        @JvmStatic
        fun mainPlanets(): List<Planet> = listOf(
            SUN, MOON, MERCURY, VENUS, MARS,
            JUPITER, SATURN, URANUS, NEPTUNE, PLUTO
        )

        /**
         * Returns classical planets only (visible to naked eye).
         */
        @JvmStatic
        fun classicalPlanets(): List<Planet> = listOf(
            SUN, MOON, MERCURY, VENUS, MARS, JUPITER, SATURN
        )

        /**
         * Returns modern planets only (requires telescope).
         */
        @JvmStatic
        fun modernPlanets(): List<Planet> = listOf(
            URANUS, NEPTUNE, PLUTO
        )
    }
}
