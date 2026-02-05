package com.medidropbox.repository;

import com.medidropbox.entity.HospitalEmergencyContact;
import com.medidropbox.entity.HospitalPatient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalEmergencyContactRepository extends JpaRepository<HospitalEmergencyContact, Long> {
    List<HospitalEmergencyContact> findByHospitalPatient(HospitalPatient hospitalPatient);
    List<HospitalEmergencyContact> findByHospitalPatientId(Long hospitalPatientId);
    List<HospitalEmergencyContact> findByHospitalPatientIdAndIsActiveTrue(Long hospitalPatientId);
    HospitalEmergencyContact findByHospitalPatientIdAndIsPrimaryTrue(Long hospitalPatientId);
}
