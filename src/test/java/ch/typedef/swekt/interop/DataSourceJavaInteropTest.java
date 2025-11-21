package ch.typedef.swekt.interop;

import ch.typedef.swekt.config.DataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java interop tests for DataSource.
 */
public class DataSourceJavaInteropTest {

    @Test
    public void shouldAccessEnumValues() {
        DataSource ephemeris = DataSource.EPHEMERIS;
        
        assertThat(ephemeris.getDisplayName()).isEqualTo("Swiss Ephemeris");
        assertThat(ephemeris.getFileExtension()).isEqualTo(".se1");
        assertThat(ephemeris.getRequiresFiles()).isTrue();
    }

    @Test
    public void shouldAccessAllEnumValues() {
        DataSource[] sources = DataSource.values();
        
        assertThat(sources).hasSize(3);
        assertThat(sources[0]).isEqualTo(DataSource.EPHEMERIS);
    }

    @Test
    public void shouldLookupByName() {
        DataSource source = DataSource.fromName("ephemeris");
        
        assertThat(source).isEqualTo(DataSource.EPHEMERIS);
    }

    @Test
    public void shouldReturnNullForUnknownName() {
        DataSource source = DataSource.fromName("unknown");
        
        assertThat(source).isNull();
    }

    @Test
    public void shouldUseInSwitchStatement() {
        DataSource source = DataSource.MOSHIER;
        String result;
        
        switch (source) {
            case EPHEMERIS:
                result = "Swiss";
                break;
            case MOSHIER:
                result = "Built-in";
                break;
            case JPL:
                result = "NASA";
                break;
            default:
                result = "Unknown";
                break;
        }
        
        assertThat(result).isEqualTo("Built-in");
    }
}
