package ch.typedef.swekt.config

import java.io.File

/**
 * Configuration for ephemeris data file resolution.
 *
 * This class manages the search paths for ephemeris data files (.se1, .se2, etc.).
 * It supports multiple path sources:
 * - Custom paths via builder
 * - SE_EPHE_PATH environment variable
 * - Classpath resources
 *
 * @property searchPaths Ordered list of paths to search for ephemeris files
 */
@ConsistentCopyVisibility
data class EphemerisConfig private constructor(
    val searchPaths: List<String>
) {
    
    companion object {
        private const val CLASSPATH_RESOURCE_PATH = "ch/typedef/swekt/data"
        
        /**
         * Creates a default configuration.
         * Includes classpath resources only.
         */
        @JvmStatic
        @JvmName("getDefault")
        fun default(): EphemerisConfig {
            return builder()
                .includeClasspath(true)
                .build()
        }
        
        /**
         * Creates configuration from SE_EPHE_PATH environment variable.
         * 
         * Supports both Unix (:) and Windows (;) path separators.
         *
         * @param envPath The SE_EPHE_PATH value
         * @return Configuration with paths from environment variable
         */
        @JvmStatic
        fun fromEnvironment(envPath: String): EphemerisConfig {
            if (envPath.isBlank()) {
                return EphemerisConfig(emptyList())
            }
            
            // Detect separator: ; for Windows, : for Unix
            val separator = if (envPath.contains(';')) ';' else ':'
            
            val paths = envPath.split(separator)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            return EphemerisConfig(paths)
        }
        
        /**
         * Creates a builder for custom configuration.
         */
        @JvmStatic
        fun builder(): Builder = Builder()
    }
    
    /**
     * Builder for EphemerisConfig.
     */
    class Builder {
        private val paths = mutableListOf<String>()
        private var includeClasspath = false
        
        /**
         * Adds a custom search path.
         *
         * @param path Path to add (file system path or URL)
         * @return This builder
         */
        fun addPath(path: String): Builder {
            paths.add(path)
            return this
        }
        
        /**
         * Configures whether to include classpath resources.
         *
         * @param include True to include classpath resources
         * @return This builder
         */
        fun includeClasspath(include: Boolean): Builder {
            includeClasspath = include
            return this
        }
        
        /**
         * Builds the configuration.
         *
         * @return Immutable EphemerisConfig
         */
        fun build(): EphemerisConfig {
            val allPaths = mutableListOf<String>()
            allPaths.addAll(paths)
            
            if (includeClasspath) {
                allPaths.add(CLASSPATH_RESOURCE_PATH)
            }
            
            return EphemerisConfig(allPaths.toList())
        }
    }
}
