package tn.devoteam.demo.detector.api;

import jakarta.validation.constraints.NotBlank;

/**
 * Données d’un événement de connexion.
 */
public record LoginEventRequest(
        @NotBlank String userId,
        @NotBlank String ip,
        @NotBlank String country,
        @NotBlank String deviceId,
        @NotBlank String timestamp
) {}
