package ch.typedef.swekt.io

/**
 * Swiss Ephemeris file formats.
 */
enum class FileFormat(
    val extension: String,
    val description: String
) {
    /**
     * Swiss Ephemeris compressed format (.se1)
     */
    SE1(".se1", "Swiss Ephemeris compressed"),
    
    /**
     * JPL binary ephemeris format (.eph)
     */
    JPL(".eph", "JPL binary ephemeris"),
    
    /**
     * Unknown or unsupported format
     */
    UNKNOWN("", "Unknown format");
    
    companion object {
        /**
         * Determines file format from extension.
         *
         * @param fileName File name or path
         * @return Detected format
         */
        @JvmStatic
        fun fromFileName(fileName: String): FileFormat {
            return when {
                fileName.endsWith(".se1", ignoreCase = true) -> SE1
                fileName.endsWith(".eph", ignoreCase = true) -> JPL
                else -> UNKNOWN
            }
        }
    }
}
