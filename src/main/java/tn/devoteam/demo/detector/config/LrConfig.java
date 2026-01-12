package tn.devoteam.demo.detector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "fraud.lr")
public record LrConfig(
        double intercept,
        Map<String, Double> weights
) {}
