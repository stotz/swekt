package ch.typedef.swekt.interop;

import ch.typedef.swekt.config.EphemerisConfig;
import ch.typedef.swekt.config.EphemerisPathResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java interop tests for EphemerisPathResolver.
 */
public class EphemerisPathResolverJavaInteropTest {

    @Test
    public void shouldCreateResolver() {
        EphemerisConfig config = EphemerisConfig.getDefault();
        EphemerisPathResolver resolver = new EphemerisPathResolver(config);
        
        assertThat(resolver).isNotNull();
    }

    @Test
    public void shouldFindFile(@TempDir Path tempDir) throws Exception {
        // Create test file
        Path testFile = tempDir.resolve("sepl_18.se1");
        Files.createFile(testFile);
        
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build();
        EphemerisPathResolver resolver = new EphemerisPathResolver(config);
        
        Path found = resolver.findFile("sepl_18.se1");
        
        assertThat(found).isNotNull();
        assertThat(found.getFileName().toString()).isEqualTo("sepl_18.se1");
    }

    @Test
    public void shouldCheckIfExists(@TempDir Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.se1");
        Files.createFile(testFile);
        
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build();
        EphemerisPathResolver resolver = new EphemerisPathResolver(config);
        
        assertThat(resolver.exists("test.se1")).isTrue();
        assertThat(resolver.exists("nonexistent.se1")).isFalse();
    }

    @Test
    public void shouldListFiles(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("file1.se1"));
        Files.createFile(tempDir.resolve("file2.se1"));
        Files.createFile(tempDir.resolve("readme.txt"));
        
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build();
        EphemerisPathResolver resolver = new EphemerisPathResolver(config);
        
        List<Path> files = resolver.listAvailableFiles("*.se1");
        
        assertThat(files).hasSize(2);
    }

    @Test
    public void shouldReturnNullForNotFound(@TempDir Path tempDir) {
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath(tempDir.toString())
            .build();
        EphemerisPathResolver resolver = new EphemerisPathResolver(config);
        
        Path found = resolver.findFile("nonexistent.se1");
        
        assertThat(found).isNull();
    }
}
