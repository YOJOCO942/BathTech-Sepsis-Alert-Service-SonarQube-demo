package com.bathtech.sepsis.repository;

import com.bathtech.sepsis.model.SepsisAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SepsisAlertRepository extends JpaRepository<SepsisAlert, Long> {
    List<SepsisAlert> findByPatientIdOrderByCreatedAtDesc(Long patientId);
    List<SepsisAlert> findByAcknowledgedFalseOrderByCreatedAtDesc();
}
