package tn.devoteam.demo.detector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.devoteam.demo.detector.api.RiskResponse;
import tn.devoteam.demo.detector.config.LrConfig;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoginAnomalyDetectorService {

    private final LoginFeatureService featureService;
    private final LrConfig config;

    public RiskResponse computeRisk(String userId, Instant at) {

        LoginFeatures f = featureService.compute(userId, at);

        double score =
                config.intercept()
                        + f.distinctIp10m() * config.weights().get("distinctIp10m")
                        + f.distinctCountries30m() * config.weights().get("distinctCountries30m")
                        + f.eventsPerMin30m() * config.weights().get("eventsPerMin30m")
                        + f.deviceChanges30m() * config.weights().get("deviceChanges30m")
                        + (f.impossibleTravel() ? 1 : 0) * config.weights().get("impossibleTravel");

        double risk = 1 / (1 + Math.exp(-score));

        String decision =
                risk > 0.7 ? "BLOCK"
                        : risk > 0.4 ? "MFA"
                        : "ALLOW";

        return new RiskResponse(risk, decision, f);
    }
}
