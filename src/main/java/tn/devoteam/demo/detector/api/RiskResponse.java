package tn.devoteam.demo.detector.api;

import tn.devoteam.demo.detector.service.LoginFeatures;

public record RiskResponse(
        double risk,
        String decision,
        LoginFeatures features
) {}
