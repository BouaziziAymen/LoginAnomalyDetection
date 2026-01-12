package tn.devoteam.demo.detector.model;

import java.time.Instant;

/**
 * Événement de connexion côté métier.
 */
public record LoginEvent(
        String userId,
        String ip,
        String country,
        String deviceId,
        Instant occurredAt
) {}
