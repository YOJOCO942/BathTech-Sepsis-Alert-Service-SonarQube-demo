package com.bathtech.sepsis.service;

import com.bathtech.sepsis.model.Patient;
import com.bathtech.sepsis.model.SepsisAlert;
import com.bathtech.sepsis.model.SepsisAlert.Severity;
import com.bathtech.sepsis.repository.SepsisAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SepsisRiskServiceTest {

    @Mock
    private SepsisAlertRepository alertRepository;

    private SepsisRiskService riskService;
    private Patient testPatient;

    @BeforeEach
    void setUp() {
        riskService = new SepsisRiskService(alertRepository);
        testPatient = new Patient("1234567890", "Jane Doe", 65, "Ward A");
    }

    @Test
    void shouldNotCreateAlertWhenScoreBelowThreshold() {
        SepsisAlert result = riskService.evaluateRisk(testPatient, 0.1);
        assertNull(result);
        verify(alertRepository, never()).save(any());
    }

    @Test
    void shouldCreateLowAlertWhenScoreAboveMinimumThreshold() {
        when(alertRepository.save(any(SepsisAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SepsisAlert result = riskService.evaluateRisk(testPatient, 0.35);
        assertNotNull(result);
        assertEquals(Severity.LOW, result.getSeverity());
    }

    @Test
    void shouldCreateCriticalAlertWhenScoreAbove90Percent() {
        when(alertRepository.save(any(SepsisAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SepsisAlert result = riskService.evaluateRisk(testPatient, 0.95);
        assertNotNull(result);
        assertEquals(Severity.CRITICAL, result.getSeverity());
    }

    @Test
    void shouldRejectScoreOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> riskService.evaluateRisk(testPatient, 1.5));
        assertThrows(IllegalArgumentException.class,
                () -> riskService.evaluateRisk(testPatient, -0.1));
    }

    @Test
    void shouldClassifyMediumSeverityCorrectly() {
        assertEquals(Severity.MEDIUM, riskService.classifySeverity(0.6));
    }

    @Test
    void shouldClassifyHighSeverityCorrectly() {
        assertEquals(Severity.HIGH, riskService.classifySeverity(0.8));
    }

    @Test
    void shouldClassifyBoundaryAsHighNotCritical() {
        // Exactly 0.7 should be HIGH, not MEDIUM
        assertEquals(Severity.HIGH, riskService.classifySeverity(0.7));
    }

    @Test
    void shouldClassifyBoundaryAsCritical() {
        // Exactly 0.9 should be CRITICAL
        assertEquals(Severity.CRITICAL, riskService.classifySeverity(0.9));
    }
}
