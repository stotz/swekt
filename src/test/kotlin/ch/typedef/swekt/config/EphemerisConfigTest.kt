package ch.typedef.swekt.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * TDD Tests for EphemerisConfig
 */
class EphemerisConfigTest {

    @Test
    fun `should create default config`() {
        val config = EphemerisConfig.default()
        
        assertThat(config).isNotNull
        assertThat(config.searchPaths).isNotEmpty
    }

    @Test
    fun `should create config with custom path`(@TempDir tempDir: Path) {
        val config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build()
        
        assertThat(config.searchPaths).contains(tempDir.toString())
    }

    @Test
    fun `should create config with multiple paths`(@TempDir tempDir: Path) {
        val path1 = tempDir.resolve("ephe1").toString()
        val path2 = tempDir.resolve("ephe2").toString()
        
        val config = EphemerisConfig.builder()
            .addPath(path1)
            .addPath(path2)
            .build()
        
        assertThat(config.searchPaths).containsExactly(path1, path2)
    }

    @Test
    fun `should parse SE_EPHE_PATH with single path`() {
        val path = "/usr/share/ephe"
        
        val config = EphemerisConfig.fromEnvironment(path)
        
        assertThat(config.searchPaths).containsExactly(path)
    }

    @Test
    fun `should parse SE_EPHE_PATH with multiple paths on Unix`() {
        val envPath = "/usr/share/ephe:/opt/ephe:/home/user/ephe"
        
        val config = EphemerisConfig.fromEnvironment(envPath)
        
        assertThat(config.searchPaths).containsExactly(
            "/usr/share/ephe",
            "/opt/ephe",
            "/home/user/ephe"
        )
    }

    @Test
    fun `should parse SE_EPHE_PATH with multiple paths on Windows`() {
        val envPath = "C:\\ephe;D:\\astro\\ephe"
        
        val config = EphemerisConfig.fromEnvironment(envPath)
        
        assertThat(config.searchPaths).containsExactly(
            "C:\\ephe",
            "D:\\astro\\ephe"
        )
    }

    @Test
    fun `should handle empty SE_EPHE_PATH`() {
        val config = EphemerisConfig.fromEnvironment("")
        
        assertThat(config.searchPaths).isEmpty()
    }

    @Test
    fun `should include classpath resource path`() {
        val config = EphemerisConfig.default()
        
        assertThat(config.searchPaths).anyMatch { it.contains("swekt/data") }
    }

    @Test
    fun `should be immutable`() {
        val config = EphemerisConfig.builder()
            .addPath("/path1")
            .build()
        
        val paths = config.searchPaths
        assertThat(paths).isInstanceOf(List::class.java)
        // Should return immutable list
    }

    @Test
    fun `should support builder pattern`() {
        val config = EphemerisConfig.builder()
            .addPath("/path1")
            .addPath("/path2")
            .includeClasspath(true)
            .build()
        
        assertThat(config.searchPaths).hasSize(3) // 2 custom + 1 classpath
    }

    @Test
    fun `should allow disabling classpath`() {
        val config = EphemerisConfig.builder()
            .addPath("/path1")
            .includeClasspath(false)
            .build()
        
        assertThat(config.searchPaths).containsExactly("/path1")
    }
}
