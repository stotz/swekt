package ch.typedef.swekt.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/**
 * TDD Tests for EphemerisPathResolver
 */
class EphemerisPathResolverTest {

    @Test
    fun `should create resolver with config`() {
        val config = EphemerisConfig.default()
        val resolver = EphemerisPathResolver(config)
        
        assertThat(resolver).isNotNull
    }

    @Test
    fun `should find file in file system`(@TempDir tempDir: Path) {
        // Create test file
        val testFile = tempDir.resolve("sepl_18.se1")
        Files.createFile(testFile)
        
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("sepl_18.se1")
        
        assertThat(found).isNotNull
        assertThat(found?.fileName.toString()).isEqualTo("sepl_18.se1")
    }

    @Test
    fun `should return null if file not found`(@TempDir tempDir: Path) {
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("nonexistent.se1")
        
        assertThat(found).isNull()
    }

    @Test
    fun `should search in order of configured paths`(@TempDir tempDir: Path) {
        val path1 = tempDir.resolve("ephe1")
        val path2 = tempDir.resolve("ephe2")
        Files.createDirectories(path1)
        Files.createDirectories(path2)
        
        // Create same file in both directories
        val file1 = path1.resolve("sepl_18.se1")
        val file2 = path2.resolve("sepl_18.se1")
        Files.writeString(file1, "version1")
        Files.writeString(file2, "version2")
        
        val config = EphemerisConfig.builder()
            .addPath(path1.toString())
            .addPath(path2.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("sepl_18.se1")
        
        assertThat(found).isNotNull
        assertThat(found?.parent).isEqualTo(path1)
    }

    @Test
    fun `should handle multiple search paths`(@TempDir tempDir: Path) {
        val path1 = tempDir.resolve("ephe1")
        val path2 = tempDir.resolve("ephe2")
        Files.createDirectories(path1)
        Files.createDirectories(path2)
        
        // File only in second path
        val file = path2.resolve("semo_18.se1")
        Files.createFile(file)
        
        val config = EphemerisConfig.builder()
            .addPath(path1.toString())
            .addPath(path2.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("semo_18.se1")
        
        assertThat(found).isNotNull
        assertThat(found?.parent).isEqualTo(path2)
    }

    @Test
    fun `should check if file exists`(@TempDir tempDir: Path) {
        val testFile = tempDir.resolve("test.se1")
        Files.createFile(testFile)
        
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        assertThat(resolver.exists("test.se1")).isTrue()
        assertThat(resolver.exists("nonexistent.se1")).isFalse()
    }

    @Test
    fun `should handle empty search paths`() {
        val config = EphemerisConfig.builder()
            .includeClasspath(false)
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("any.se1")
        
        assertThat(found).isNull()
    }

    @Test
    fun `should handle invalid paths gracefully`() {
        val config = EphemerisConfig.builder()
            .addPath("/nonexistent/path/that/does/not/exist")
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("test.se1")
        
        assertThat(found).isNull()
    }

    @Test
    fun `should list available files in search paths`(@TempDir tempDir: Path) {
        Files.createFile(tempDir.resolve("sepl_18.se1"))
        Files.createFile(tempDir.resolve("semo_18.se1"))
        Files.createFile(tempDir.resolve("seas_18.se1"))
        Files.createFile(tempDir.resolve("readme.txt")) // Should be ignored
        
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val files = resolver.listAvailableFiles("*.se1")
        
        assertThat(files).hasSize(3)
        assertThat(files.map { it.fileName.toString() }).containsExactlyInAnyOrder(
            "sepl_18.se1",
            "semo_18.se1",
            "seas_18.se1"
        )
    }

    @Test
    fun `should resolve relative path`(@TempDir tempDir: Path) {
        val subDir = tempDir.resolve("data")
        Files.createDirectories(subDir)
        Files.createFile(subDir.resolve("test.se1"))
        
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        val found = resolver.findFile("data/test.se1")
        
        assertThat(found).isNotNull
        assertThat(found?.fileName.toString()).isEqualTo("test.se1")
    }

    @Test
    fun `should normalize paths with backslashes`(@TempDir tempDir: Path) {
        val subDir = tempDir.resolve("data")
        Files.createDirectories(subDir)
        Files.createFile(subDir.resolve("test.se1"))
        
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        val resolver = EphemerisPathResolver(config)
        
        // Windows-style path
        val found = resolver.findFile("data\\test.se1")
        
        assertThat(found).isNotNull
    }
}
