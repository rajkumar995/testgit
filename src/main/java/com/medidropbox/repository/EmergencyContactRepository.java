package com.medidropbox.repository;

import com.medidropbox.entity.EmergencyContact;
import com.medidropbox.entity.GlobalPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByGlobalPatient(GlobalPatient globalPatient);
    List<EmergencyContact> findByGlobalPatientId(Long globalPatientId);
    List<EmergencyContact> findByGlobalPatientIdAndIsActiveTrue(Long globalPatientId);
    EmergencyContact findByGlobalPatientIdAndIsPrimaryTrue(Long globalPatientId);
}
