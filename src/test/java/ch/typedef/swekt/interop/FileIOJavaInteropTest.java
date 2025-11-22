package ch.typedef.swekt.interop;

import ch.typedef.swekt.io.EphemerisFileHeader;
import ch.typedef.swekt.io.FileFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java interop tests for file I/O classes.
 */
public class FileIOJavaInteropTest {

    @Test
    public void shouldDetectFileFormat() {
        FileFormat se1 = FileFormat.fromFileName("sepl_18.se1");
        FileFormat jpl = FileFormat.fromFileName("de441.eph");
        
        assertThat(se1).isEqualTo(FileFormat.SE1);
        assertThat(jpl).isEqualTo(FileFormat.JPL);
    }

    @Test
    public void shouldReadFileHeader(@TempDir Path tempDir) throws Exception {
        Path testFile = createTestFile(tempDir);
        
        EphemerisFileHeader header = EphemerisFileHeader.read(testFile);
        
        assertThat(header).isNotNull();
        assertThat(header.getMagic()).isEqualTo("SEPL");
        assertThat(header.getFileFormat()).isEqualTo(FileFormat.SE1);
    }

    @Test
    public void shouldAccessHeaderProperties(@TempDir Path tempDir) throws Exception {
        Path testFile = createTestFile(tempDir);
        
        EphemerisFileHeader header = EphemerisFileHeader.read(testFile);
        
        assertThat(header.getVersion()).isGreaterThan(0);
        assertThat(header.getStartJD()).isGreaterThan(0);
        assertThat(header.getEndJD()).isGreaterThan(header.getStartJD());
        assertThat(header.getByteOrder()).isNotNull();
        assertThat(header.getAstronomicalUnit()).isBetween(1.49e8, 1.50e8);
    }

    private Path createTestFile(Path tempDir) throws Exception {
        Path testFile = tempDir.resolve("test.se1");
        
        ByteBuffer buffer = ByteBuffer.allocate(1024)
            .order(ByteOrder.LITTLE_ENDIAN);
        
        buffer.put("SEPL".getBytes());
        buffer.putInt(210);
        buffer.putDouble(2451545.0);
        buffer.putDouble(2488069.0);
        buffer.putInt(400);
        buffer.putDouble(149597870.7);
        buffer.putDouble(81.30056907419062);
        buffer.putInt(256);
        
        Files.write(testFile, buffer.array());
        return testFile;
    }
}
