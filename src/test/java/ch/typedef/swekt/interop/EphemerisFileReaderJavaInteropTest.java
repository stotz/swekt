package ch.typedef.swekt.interop;

import ch.typedef.swekt.config.EphemerisConfig;
import ch.typedef.swekt.io.EphemerisFileHeader;
import ch.typedef.swekt.io.EphemerisFileReader;
import ch.typedef.swekt.io.EphemerisRecord;
import ch.typedef.swekt.io.FileFormat;
import ch.typedef.swekt.model.JulianDay;
import ch.typedef.swekt.model.Planet;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("EphemerisFileReader Java Interop")
public class EphemerisFileReaderJavaInteropTest {

    @Test
    void readerShouldBeAccessibleFromJava() {
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath("/tmp/ephe")
            .build();
        
        EphemerisFileReader reader = new EphemerisFileReader(config);
        
        assertThat(reader).isNotNull();
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    void shouldReadHeaderFromJava() {
        EphemerisConfig config = EphemerisConfig.fromEnvironment();
        EphemerisFileReader reader = new EphemerisFileReader(config);
        
        EphemerisFileHeader header = reader.readHeader(Planet.MARS);
        
        assertThat(header).isNotNull();
        assertThat(header.getFileFormat()).isEqualTo(FileFormat.SE1);
        assertThat(header.getStartJD()).isGreaterThan(0.0);
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    void shouldReadRecordFromJava() {
        EphemerisConfig config = EphemerisConfig.fromEnvironment();
        EphemerisFileReader reader = new EphemerisFileReader(config);
        JulianDay jd = JulianDay.getJ2000();
        
        EphemerisRecord record = reader.readRecord(Planet.MARS, jd);
        
        assertThat(record).isNotNull();
        assertThat(record.getJulianDay()).isEqualTo(jd);
        assertThat(record.getPosition()).hasSize(3);
        assertThat(record.getVelocity()).hasSize(3);
    }

    @Test
    @Disabled("Requires full binary SE1 format implementation")
    @EnabledIfEnvironmentVariable(named = "SE_EPHE_PATH", matches = ".+")
    void shouldCheckAvailabilityFromJava() {
        EphemerisConfig config = EphemerisConfig.fromEnvironment();
        EphemerisFileReader reader = new EphemerisFileReader(config);
        JulianDay jd = JulianDay.getJ2000();
        
        boolean available = reader.isAvailable(Planet.MARS, jd);
        
        assertThat(available).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenFileNotFoundFromJava() {
        EphemerisConfig config = EphemerisConfig.builder()
            .addPath("/nonexistent")
            .build();
        EphemerisFileReader reader = new EphemerisFileReader(config);
        
        assertThrows(IllegalArgumentException.class, () -> {
            reader.readHeader(Planet.MARS);
        });
    }
}
