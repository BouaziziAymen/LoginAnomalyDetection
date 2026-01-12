package tn.devoteam.demo.detector.data;

import org.springframework.data.annotation.Id;

/**
 * Document Couchbase représentant un événement de connexion.
 */
public record LoginEventDocument(
        @Id String id,
        String type,
        String userId,
        String ip,
        String country,
        String deviceId,
        long tsEpoch
) {}
