package tn.devoteam.demo.detector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.devoteam.demo.detector.data.LoginEventDocument;
import tn.devoteam.demo.detector.data.LoginEventReadRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginFeatureService {

    private final LoginEventReadRepository readRepository;

    /**
     * Compute fraud-related features for a user at a given time.
     */
    public LoginFeatures compute(String userId, Instant at) {

        // --- Fetch events ---
        List<LoginEventDocument> last10m =
                readRepository.findSince(
                        userId,
                        at.minus(Duration.ofMinutes(10)).getEpochSecond()
                );

        List<LoginEventDocument> last30m =
                readRepository.findSince(
                        userId,
                        at.minus(Duration.ofMinutes(30)).getEpochSecond()
                );

        // --- Feature: distinct IPs (10 min) ---
        int distinctIp10m = (int) last10m.stream()
                .map(LoginEventDocument::ip)
                .distinct()
                .count();

        // --- Feature: distinct countries (30 min) ---
        int distinctCountries30m = (int) last30m.stream()
                .map(LoginEventDocument::country)
                .distinct()
                .count();

        // --- Feature: device changes (30 min) ---
        int deviceChanges30m = Math.max(
                0,
                (int) last30m.stream()
                        .map(LoginEventDocument::deviceId)
                        .distinct()
                        .count() - 1
        );

        // --- Feature: events per minute (30 min) ---
        double eventsPerMin30m = last30m.size() / 30.0;

        // --- Feature: impossible travel ---
        boolean impossibleTravel = false;
        if (distinctCountries30m > 1 && last30m.size() >= 2) {
            long first = last30m.get(0).tsEpoch();
            long last = last30m.get(last30m.size() - 1).tsEpoch();
            long minutes = Duration.ofSeconds(last - first).toMinutes();
            impossibleTravel = minutes < 60;
        }

        return new LoginFeatures(
                distinctIp10m,
                distinctCountries30m,
                eventsPerMin30m,
                deviceChanges30m,
                impossibleTravel
        );
    }
}
