package tn.devoteam.demo.detector.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.devoteam.demo.detector.model.LoginEvent;
import tn.devoteam.demo.detector.service.LoginEventService;

import java.time.Instant;
import java.util.Map;

/**
 * API REST pour les événements de connexion.
 */
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class LoginController {

    private final LoginEventService loginEventService;

    /**
     * Réception d’un événement de login.
     */
    @PostMapping("/login")
    public Map<String, Object> ingestLogin(@Valid @RequestBody LoginEventRequest request) {

        LoginEvent event = new LoginEvent(
                request.userId(),
                request.ip(),
                request.country(),
                request.deviceId(),
                Instant.parse(request.timestamp())
        );

        loginEventService.persist(event);

        return Map.of(
                "status", "OK",
                "receivedAt", Instant.now().toString()
        );
    }
}
