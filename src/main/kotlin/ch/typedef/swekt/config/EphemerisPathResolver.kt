package ch.typedef.swekt.config

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries

/**
 * Resolves ephemeris data files from configured search paths.
 *
 * This class handles finding ephemeris files in:
 * - File system directories
 * - Classpath resources (future)
 *
 * @property config The configuration containing search paths
 */
class EphemerisPathResolver(
    private val config: EphemerisConfig
) {
    
    /**
     * Finds an ephemeris file by name.
     *
     * Searches all configured paths in order and returns the first match.
     *
     * @param fileName Name of the file to find (e.g., "sepl_18.se1")
     * @return Path to the file, or null if not found
     */
    fun findFile(fileName: String): Path? {
        val normalizedFileName = normalizeFileName(fileName)
        
        for (searchPath in config.searchPaths) {
            val found = searchInPath(searchPath, normalizedFileName)
            if (found != null) {
                return found
            }
        }
        
        return null
    }
    
    /**
     * Checks if a file exists in any of the configured paths.
     *
     * @param fileName Name of the file to check
     * @return True if file exists
     */
    fun exists(fileName: String): Boolean {
        return findFile(fileName) != null
    }
    
    /**
     * Lists all available files matching a pattern in search paths.
     *
     * @param pattern File pattern (e.g., "*.se1")
     * @return List of matching files
     */
    fun listAvailableFiles(pattern: String): List<Path> {
        val files = mutableListOf<Path>()
        val glob = pattern.replace("*", ".*").toRegex()
        
        for (searchPath in config.searchPaths) {
            val path = Paths.get(searchPath)
            if (Files.exists(path) && Files.isDirectory(path)) {
                try {
                    path.listDirectoryEntries()
                        .filter { it.isRegularFile() }
                        .filter { glob.matches(it.fileName.toString()) }
                        .forEach { files.add(it) }
                } catch (e: Exception) {
                    // Skip directories that can't be read
                }
            }
        }
        
        return files
    }
    
    /**
     * Searches for a file in a specific path.
     *
     * @param searchPath Path to search in
     * @param fileName File name (possibly with subdirectory)
     * @return Path if found, null otherwise
     */
    private fun searchInPath(searchPath: String, fileName: String): Path? {
        try {
            val basePath = Paths.get(searchPath)
            if (!basePath.exists()) {
                return null
            }
            
            val fullPath = basePath.resolve(fileName)
            return if (fullPath.exists() && fullPath.isRegularFile()) {
                fullPath
            } else {
                null
            }
        } catch (e: Exception) {
            // Invalid path or access denied
            return null
        }
    }
    
    /**
     * Normalizes file name to use forward slashes.
     *
     * @param fileName File name to normalize
     * @return Normalized file name
     */
    private fun normalizeFileName(fileName: String): String {
        return fileName.replace('\\', '/')
    }
}
