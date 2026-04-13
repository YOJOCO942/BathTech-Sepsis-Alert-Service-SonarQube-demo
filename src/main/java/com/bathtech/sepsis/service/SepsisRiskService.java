package com.bathtech.sepsis.service;

import com.bathtech.sepsis.model.Patient;
import com.bathtech.sepsis.model.SepsisAlert;
import com.bathtech.sepsis.model.SepsisAlert.Severity;
import com.bathtech.sepsis.repository.SepsisAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Processes risk scores from the AI inference service and
 * generates sepsis alerts when thresholds are exceeded.
 */
@Service
public class SepsisRiskService {

    private static final Logger logger = LoggerFactory.getLogger(SepsisRiskService.class);

    private static final double THRESHOLD_LOW = 0.3;
    private static final double THRESHOLD_MEDIUM = 0.5;
    private static final double THRESHOLD_HIGH = 0.7;
    private static final double THRESHOLD_CRITICAL = 0.9;

    private final SepsisAlertRepository alertRepository;

    public SepsisRiskService(SepsisAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Evaluate a risk score returned by the AI model and create
     * an alert if the score exceeds the minimum threshold.
     *
     * @param patient   the patient being assessed
     * @param riskScore probability of sepsis (0.0 to 1.0)
     * @return the created alert, or null if score is below threshold
     */
    public SepsisAlert evaluateRisk(Patient patient, double riskScore) {
        if (riskScore < 0.0 || riskScore > 1.0) {
            throw new IllegalArgumentException("Risk score must be between 0.0 and 1.0");
        }

        if (riskScore < THRESHOLD_LOW) {
            logger.debug("Patient {} risk score {} below threshold, no alert",
                    patient.getNhsNumber(), riskScore);
            return null;
        }

        SepsisAlert alert = new SepsisAlert();
        alert.setPatient(patient);
        alert.setRiskScore(riskScore);
        alert.setSeverity(classifySeverity(riskScore));

        SepsisAlert saved = alertRepository.save(alert);
        logger.warn("Sepsis alert created: patient={}, severity={}, score={}",
                patient.getNhsNumber(), saved.getSeverity(), riskScore);

        return saved;
    }

    /**
     * Map a numeric risk score to a severity level.
     */
    Severity classifySeverity(double riskScore) {
        if (riskScore >= THRESHOLD_CRITICAL) {
            return Severity.CRITICAL;
        } else if (riskScore >= THRESHOLD_HIGH) {
            return Severity.HIGH;
        } else if (riskScore >= THRESHOLD_MEDIUM) {
            return Severity.MEDIUM;
        } else {
            return Severity.LOW;
        }
    }
}
