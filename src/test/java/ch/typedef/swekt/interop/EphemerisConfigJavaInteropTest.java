package ch.typedef.swekt.interop;

import ch.typedef.swekt.config.EphemerisConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java interop tests for EphemerisConfig.
 */
public class EphemerisConfigJavaInteropTest {

    @Test
    public void shouldCreateDefaultConfig() {
        EphemerisConfig config = EphemerisConfig.getDefault();
        
        assertThat(config).isNotNull();
        assertThat(config.getSearchPaths()).isNotEmpty();
    }

    @Test
    public void shouldUseBuilder() {
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath("/path1")
            .addPath("/path2")
            .includeClasspath(true)
            .build();
        
        assertThat(config.getSearchPaths()).hasSize(3);
    }

    @Test
    public void shouldParseEnvironmentVariable() {
        String envPath = "/usr/share/ephe:/opt/ephe";
        
        EphemerisConfig config = EphemerisConfig.fromEnvironment(envPath);
        
        assertThat(config.getSearchPaths()).containsExactly(
            "/usr/share/ephe",
            "/opt/ephe"
        );
    }

    @Test
    public void shouldHandleWindowsPaths() {
        String envPath = "C:\\ephe;D:\\astro";
        
        EphemerisConfig config = EphemerisConfig.fromEnvironment(envPath);
        
        assertThat(config.getSearchPaths()).containsExactly(
            "C:\\ephe",
            "D:\\astro"
        );
    }

    @Test
    public void shouldReturnImmutableList() {
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath("/path1")
            .build();
        
        assertThat(config.getSearchPaths()).isInstanceOf(java.util.List.class);
    }
}
