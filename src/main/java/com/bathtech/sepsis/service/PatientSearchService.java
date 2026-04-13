package com.bathtech.sepsis.service;

import com.bathtech.sepsis.model.Patient;
import com.bathtech.sepsis.model.SepsisAlert;
import com.bathtech.sepsis.repository.PatientRepository;
import com.bathtech.sepsis.repository.SepsisAlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * FIXED VERSION — passes SonarQube quality gate
 * All security vulnerabilities from the vulnerable version
 * have been resolved.
 * ============================================================
 *
 * Fixes applied:
 *   1. SQL injection  → parameterised queries via Spring Data JPA
 *   2. Hardcoded creds → removed; use environment variables
 *   3. System.out      → SLF4J logger
 *   4. Empty catch     → proper exception handling with logging
 *   5. Returning null  → return Collections.emptyList()
 */
@Service
public class PatientSearchService {

    private static final Logger logger = LoggerFactory.getLogger(PatientSearchService.class);

    private final PatientRepository patientRepository;
    private final SepsisAlertRepository alertRepository;

    public PatientSearchService(PatientRepository patientRepository,
                                SepsisAlertRepository alertRepository) {
        this.patientRepository = patientRepository;
        this.alertRepository = alertRepository;
    }

    /**
     * FIX 1: Parameterised query via Spring Data JPA.
     * No user input ever touches the SQL string directly.
     */
    public Optional<Patient> searchByNhsNumber(String nhsNumber) {
        return patientRepository.findByNhsNumber(nhsNumber);
    }

    /**
     * FIX 2: Uses repository method — JPA generates parameterised SQL.
     */
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * FIX 3: SLF4J logger instead of System.out.
     * NHS number is masked to avoid exposing PII in logs.
     */
    public void logPatientAccess(String nhsNumber, String accessedBy) {
        String masked = nhsNumber.substring(0, 3) + "****"
                      + nhsNumber.substring(nhsNumber.length() - 3);
        logger.info("Patient {} accessed by {}", masked, accessedBy);
    }

    /**
     * FIX 4: Exception is logged, not swallowed silently.
     */
    public long countPatients() {
        try {
            return patientRepository.count();
        } catch (Exception e) {
            logger.error("Failed to count patients", e);
            return 0;
        }
    }

    /**
     * FIX 5: Returns empty list instead of null.
     */
    public List<SepsisAlert> findRecentAlerts(Long patientId) {
        try {
            return alertRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        } catch (Exception e) {
            logger.error("Failed to retrieve alerts for patient {}", patientId, e);
            return Collections.emptyList();
        }
    }
}
