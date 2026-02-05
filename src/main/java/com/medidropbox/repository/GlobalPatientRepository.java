package com.medidropbox.repository;

import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.enums.PatientStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalPatientRepository extends JpaRepository<GlobalPatient, Long> {
    Optional<GlobalPatient> findByPhone(String phone);
    Optional<GlobalPatient> findByEmail(String email);
    Optional<GlobalPatient> findByAadharId(String aadharId);
    Optional<GlobalPatient> findByAbhaId(String abhaId);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByAadharId(String aadharId);
    boolean existsByAbhaId(String abhaId);
    List<GlobalPatient> findAllByIsActiveTrue();
    List<GlobalPatient> findByStatus(PatientStatus status);
    Optional<GlobalPatient> findByPhoneAndStatus(String phone, PatientStatus status);
    
    // Eagerly fetch address and emergency contacts
    @Query("SELECT gp FROM GlobalPatient gp LEFT JOIN FETCH gp.address LEFT JOIN FETCH gp.emergencyContacts WHERE gp.id = :id")
    Optional<GlobalPatient> findByIdWithAddressAndContacts(@Param("id") Long id);
}
