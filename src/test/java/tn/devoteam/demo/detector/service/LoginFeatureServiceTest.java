package tn.devoteam.demo.detector.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.devoteam.demo.detector.data.LoginEventDocument;
import tn.devoteam.demo.detector.data.LoginEventReadRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginFeatureServiceTest {

    @Mock
    private LoginEventReadRepository readRepository;

    private LoginFeatureService service;

    @BeforeEach
    void setUp() {
        service = new LoginFeatureService(readRepository);
    }

    @Test
    void compute_callsRepositoryWithCorrectSinceEpochs() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        when(readRepository.findSince(userId, since10m)).thenReturn(List.of());
        when(readRepository.findSince(userId, since30m)).thenReturn(List.of());

        service.compute(userId, at);

        verify(readRepository).findSince(userId, since10m);
        verify(readRepository).findSince(userId, since30m);
        verifyNoMoreInteractions(readRepository);
    }

    @Test
    void compute_distinctIp10m_isCalculatedFromLast10mOnly() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        List<LoginEventDocument> last10m = List.of(
                event(userId, "41.224.10.7", "TN", "d1", 1000),
                event(userId, "41.224.10.7", "TN", "d1", 1001),
                event(userId, "10.0.0.1",    "TN", "d1", 1002)
        );

        // must exist because service always queries last30m too
        List<LoginEventDocument> last30m = List.of(
                event(userId, "9.9.9.9", "TN", "d1", 900)
        );

        when(readRepository.findSince(userId, since10m)).thenReturn(last10m);
        when(readRepository.findSince(userId, since30m)).thenReturn(last30m);

        LoginFeatures f = service.compute(userId, at);

        assertEquals(2, f.distinctIp10m());
    }

    @Test
    void compute_countries_deviceChanges_eventsPerMinute_areCorrect() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        List<LoginEventDocument> last10m = List.of(
                event(userId, "1.1.1.1", "TN", "d1", 1000)
        );

        List<LoginEventDocument> last30m = List.of(
                event(userId, "10.0.0.1", "TN", "d1", 1000),
                event(userId, "10.0.0.2", "TN", "d2", 1005),
                event(userId, "10.0.0.3", "FR", "d2", 1010),
                event(userId, "10.0.0.4", "FR", "d3", 1015),
                event(userId, "10.0.0.5", "TN", "d3", 1020),
                event(userId, "10.0.0.6", "TN", "d1", 1025)
        );

        when(readRepository.findSince(userId, since10m)).thenReturn(last10m);
        when(readRepository.findSince(userId, since30m)).thenReturn(last30m);

        LoginFeatures f = service.compute(userId, at);

        assertEquals(2, f.distinctCountries30m());
        assertEquals(2, f.deviceChanges30m());
        assertEquals(0.2, f.eventsPerMin30m(), 1e-9); // 6/30
    }

    @Test
    void compute_impossibleTravel_true_whenMultipleCountries_and_under60min_betweenFirstAndLast() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        List<LoginEventDocument> last10m = List.of();

        List<LoginEventDocument> last30m = List.of(
                event(userId, "1.1.1.1", "TN", "d1", 1000),
                event(userId, "2.2.2.2", "FR", "d1", 1000 + 30 * 60)
        );

        when(readRepository.findSince(userId, since10m)).thenReturn(last10m);
        when(readRepository.findSince(userId, since30m)).thenReturn(last30m);

        LoginFeatures f = service.compute(userId, at);

        assertTrue(f.impossibleTravel());
    }

    @Test
    void compute_impossibleTravel_false_whenOnlyOneEvent() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        when(readRepository.findSince(userId, since10m)).thenReturn(List.of());
        when(readRepository.findSince(userId, since30m)).thenReturn(List.of(
                event(userId, "1.1.1.1", "TN", "d1", 1000)
        ));

        LoginFeatures f = service.compute(userId, at);
        assertFalse(f.impossibleTravel());
    }

    @Test
    void compute_impossibleTravel_false_whenSameCountry() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        when(readRepository.findSince(userId, since10m)).thenReturn(List.of());
        when(readRepository.findSince(userId, since30m)).thenReturn(List.of(
                event(userId, "1.1.1.1", "TN", "d1", 1000),
                event(userId, "2.2.2.2", "TN", "d1", 1000 + 10 * 60)
        ));

        LoginFeatures f = service.compute(userId, at);
        assertFalse(f.impossibleTravel());
    }

    @Test
    void compute_impossibleTravel_false_when60minOrMore() {
        Instant at = Instant.parse("2026-01-12T21:10:00Z");
        String userId = "u123";

        long since10m = at.minus(Duration.ofMinutes(10)).getEpochSecond();
        long since30m = at.minus(Duration.ofMinutes(30)).getEpochSecond();

        when(readRepository.findSince(userId, since10m)).thenReturn(List.of());
        when(readRepository.findSince(userId, since30m)).thenReturn(List.of(
                event(userId, "1.1.1.1", "TN", "d1", 1000),
                event(userId, "2.2.2.2", "FR", "d1", 1000 + 60 * 60) // exactly 60 => false
        ));

        LoginFeatures f = service.compute(userId, at);
        assertFalse(f.impossibleTravel());
    }

    /**
     * Builds a real LoginEventDocument without mocking.
     * Works if LoginEventDocument is a record or a normal class with matching fields/constructor.
     */
    private static LoginEventDocument event(String userId, String ip, String country, String deviceId, long tsEpoch) {
        try {
            Class<LoginEventDocument> c = LoginEventDocument.class;

            // If it's a record, build by component name (most robust)
            if (c.isRecord()) {
                RecordComponent[] comps = c.getRecordComponents();
                Object[] args = new Object[comps.length];

                for (int i = 0; i < comps.length; i++) {
                    String name = comps[i].getName();
                    Class<?> t = comps[i].getType();

                    args[i] = switch (name) {
                        case "userId" -> userId;
                        case "ip" -> ip;
                        case "country" -> country;
                        case "deviceId" -> deviceId;
                        case "tsEpoch" -> tsEpoch;
                        case "type" -> "login_event";
                        default -> defaultValue(t);
                    };
                }

                @SuppressWarnings("unchecked")
                Constructor<LoginEventDocument> ctor =
                        (Constructor<LoginEventDocument>) c.getDeclaredConstructor(
                                java.util.Arrays.stream(comps).map(RecordComponent::getType).toArray(Class[]::new)
                        );

                ctor.setAccessible(true);
                return ctor.newInstance(args);
            }

            // Fallback: try to find any constructor we can satisfy
            for (Constructor<?> ctor : c.getDeclaredConstructors()) {
                Class<?>[] pt = ctor.getParameterTypes();
                Object[] args = new Object[pt.length];

                boolean ok = true;
                for (int i = 0; i < pt.length; i++) {
                    if (pt[i] == String.class) {
                        // best-effort order for strings
                        // userId, ip, country, deviceId, type, id...
                        args[i] = guessStringArg(i, userId, ip, country, deviceId);
                    } else if (pt[i] == long.class || pt[i] == Long.class) {
                        args[i] = tsEpoch;
                    } else {
                        args[i] = defaultValue(pt[i]);
                    }
                }

                ctor.setAccessible(true);
                if (ok) {
                    return (LoginEventDocument) ctor.newInstance(args);
                }
            }

            throw new IllegalStateException("No usable constructor found for LoginEventDocument");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create LoginEventDocument for tests", e);
        }
    }

    private static Object guessStringArg(int index, String userId, String ip, String country, String deviceId) {
        return switch (index) {
            case 0 -> userId;
            case 1 -> ip;
            case 2 -> country;
            case 3 -> deviceId;
            case 4 -> "login_event";
            default -> null;
        };
    }

    private static Object defaultValue(Class<?> t) {
        if (!t.isPrimitive()) return null;
        if (t == boolean.class) return false;
        if (t == byte.class) return (byte) 0;
        if (t == short.class) return (short) 0;
        if (t == int.class) return 0;
        if (t == long.class) return 0L;
        if (t == float.class) return 0f;
        if (t == double.class) return 0d;
        if (t == char.class) return '\0';
        return null;
    }
}
