package ch.typedef.swekt.config

/**
 * Ephemeris data sources.
 *
 * Specifies where planetary calculation data comes from.
 */
enum class DataSource(
    val displayName: String,
    val description: String,
    val fileExtension: String?,
    val requiresFiles: Boolean
) {
    /**
     * Swiss Ephemeris compressed files (.se1).
     * High precision, covers long time spans.
     */
    EPHEMERIS(
        displayName = "Swiss Ephemeris",
        description = "Swiss Ephemeris compressed ephemeris files",
        fileExtension = ".se1",
        requiresFiles = true
    ),
    
    /**
     * Moshier analytical ephemeris.
     * Built-in calculations, no external files needed.
     * Lower precision than Swiss Ephemeris but sufficient for most applications.
     */
    MOSHIER(
        displayName = "Moshier",
        description = "Moshier analytical ephemeris (built-in)",
        fileExtension = null,
        requiresFiles = false
    ),
    
    /**
     * JPL ephemeris files (.eph).
     * Highest precision, NASA/JPL data.
     */
    JPL(
        displayName = "JPL Ephemeris",
        description = "JPL DE ephemeris files",
        fileExtension = ".eph",
        requiresFiles = true
    );
    
    companion object {
        /**
         * Finds a data source by name (case-insensitive).
         *
         * @param name Name to look up
         * @return DataSource or null if not found
         */
        @JvmStatic
        fun fromName(name: String): DataSource? {
            return entries.firstOrNull { 
                it.name.equals(name, ignoreCase = true) 
            }
        }
    }
}
