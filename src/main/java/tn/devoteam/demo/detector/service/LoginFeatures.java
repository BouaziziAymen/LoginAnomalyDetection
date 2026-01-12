package tn.devoteam.demo.detector.service;

public record LoginFeatures(
        int distinctIp10m,
        int distinctCountries30m,
        double eventsPerMin30m,
        int deviceChanges30m,
        boolean impossibleTravel
) {}
