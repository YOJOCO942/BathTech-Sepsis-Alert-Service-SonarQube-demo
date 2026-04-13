package com.bathtech.sepsis.controller;

import com.bathtech.sepsis.model.SepsisAlert;
import com.bathtech.sepsis.repository.SepsisAlertRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST API for clinical staff to view and acknowledge sepsis alerts.
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final SepsisAlertRepository alertRepository;

    public AlertController(SepsisAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * GET /api/alerts/unacknowledged
     * Returns all alerts that have not yet been acknowledged by clinical staff.
     */
    @GetMapping("/unacknowledged")
    public List<SepsisAlert> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledgedFalseOrderByCreatedAtDesc();
    }

    /**
     * PUT /api/alerts/{id}/acknowledge
     * Clinical staff marks an alert as reviewed.
     */
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<SepsisAlert> acknowledgeAlert(@PathVariable Long id) {
        return alertRepository.findById(id)
                .map(alert -> {
                    alert.setAcknowledged(true);
                    return ResponseEntity.ok(alertRepository.save(alert));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
