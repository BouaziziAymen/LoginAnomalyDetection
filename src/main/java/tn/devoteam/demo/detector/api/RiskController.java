package tn.devoteam.demo.detector.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.devoteam.demo.detector.data.LoginEventStatsDao;
import tn.devoteam.demo.detector.service.LoginAnomalyDetectorService;

import java.time.Instant;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class RiskController {

    private final LoginAnomalyDetectorService detector;
    private final LoginEventStatsDao statsDao;

    @GetMapping("/{userId}/risk")
    public RiskResponse risk(
            @PathVariable String userId,
            @RequestParam(required = false) Instant at
    ) {

        Instant evalAt;

        if (at != null) {
            evalAt = at;
        } else {
            evalAt = statsDao.findLastLoginEpoch(userId)
                    .map(Instant::ofEpochSecond)
                    .orElse(Instant.now());
        }

        return detector.computeRisk(userId, evalAt);
    }
}
