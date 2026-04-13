package com.bathtech.sepsis.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ============================================================
 * VULNERABLE VERSION — for SonarQube demo only
 * This file contains DELIBERATE security flaws that SonarQube
 * will detect. Do NOT use in production.
 * ============================================================
 *
 * SonarQube rules triggered:
 *   - java:S2077  (SQL injection via string concatenation)
 *   - java:S3649  (SQL injection — taint analysis)
 *   - java:S1192  (String literals duplicated)
 *   - java:S106   (System.out instead of logger)
 *   - java:S1118  (Utility class missing private constructor — if applicable)
 */
@Service
public class PatientSearchService_VULNERABLE {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * BUG 1: SQL INJECTION (Critical — Security Vulnerability)
     * SonarQube rule: java:S2077 / java:S3649
     *
     * User input is concatenated directly into a SQL query string.
     * An attacker could submit nhsNumber = "' OR '1'='1" to
     * retrieve all patient records.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> searchByNhsNumber(String nhsNumber) {
        // VULNERABLE: raw string concatenation with user input
        String sql = "SELECT * FROM patients WHERE nhs_number = '" + nhsNumber + "'";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    /**
     * BUG 2: SECOND SQL INJECTION — search by ward
     * Same class of vulnerability, different entry point.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> searchByWard(String wardName) {
        // VULNERABLE: raw string concatenation
        String sql = "SELECT * FROM patients WHERE ward = '" + wardName + "'";
        return entityManager.createNativeQuery(sql).getResultList();
    }

    /**
     * BUG 3: HARDCODED CREDENTIALS (Critical — Security Vulnerability)
     * SonarQube rule: java:S6437 / java:S2068
     *
     * Database password stored as a string literal in source code.
     */
    private static final String DB_PASSWORD = "BathTech2026!";

    public boolean authenticateAdmin(String inputPassword) {
        return DB_PASSWORD.equals(inputPassword);
    }

    /**
     * BUG 4: USING System.out INSTEAD OF LOGGER (Code Smell — Major)
     * SonarQube rule: java:S106
     *
     * Production code should use SLF4J / Logback, not System.out.
     * Also exposes patient data in plain text in logs.
     */
    public void logPatientAccess(String nhsNumber, String accessedBy) {
        System.out.println("Patient " + nhsNumber + " accessed by " + accessedBy);
    }

    /**
     * BUG 5: EMPTY CATCH BLOCK (Code Smell — Major)
     * SonarQube rule: java:S108
     *
     * Exception is silently swallowed — failures will be invisible.
     */
    public int countPatientsInWard(String ward) {
        try {
            String sql = "SELECT COUNT(*) FROM patients WHERE ward = '" + ward + "'";
            Number result = (Number) entityManager.createNativeQuery(sql).getSingleResult();
            return result.intValue();
        } catch (Exception e) {
            // BUG: empty catch block — error silently ignored
        }
        return 0;
    }

    /**
     * BUG 6: RETURNING NULL INSTEAD OF OPTIONAL/EMPTY (Code Smell)
     * SonarQube rule: java:S1168
     *
     * Methods that return collections should return empty list, not null.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> findRecentAlerts(String patientId) {
        try {
            String sql = "SELECT * FROM sepsis_alerts WHERE patient_id = " + patientId
                       + " ORDER BY created_at DESC";
            return entityManager.createNativeQuery(sql).getResultList();
        } catch (Exception e) {
            return null;  // BUG: should return Collections.emptyList()
        }
    }
}
