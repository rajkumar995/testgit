package com.medidropbox.repository;

import com.medidropbox.entity.VitalsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalsRecordRepository extends JpaRepository<VitalsRecord, Long> {
    
    List<VitalsRecord> findByGlobalPatientIdOrderByRecordedAtDesc(Long globalPatientId);
    
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND (:startDate IS NULL OR DATE(vr.recordedAt) >= :startDate) " +
           "AND (:endDate IS NULL OR DATE(vr.recordedAt) <= :endDate) " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC")
    List<VitalsRecord> findByPatientIdAndDateRange(
        @Param("patientId") Long patientId,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate
    );
    
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.weightKg IS NOT NULL " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestWeightByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.bloodGlucoseMgdl IS NOT NULL " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestBloodGlucoseByPatientId(@Param("patientId") Long patientId);
    
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.bloodPressureSystolic IS NOT NULL " +
           "AND vr.bloodPressureDiastolic IS NOT NULL " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestBloodPressureByPatientId(@Param("patientId") Long patientId);
    
    Optional<VitalsRecord> findByIdAndGlobalPatientId(Long id, Long globalPatientId);
    
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.id = :recordId")
    Optional<VitalsRecord> findByIdAndPatientId(@Param("recordId") Long recordId, @Param("patientId") Long patientId);

    /** Hospital context: only records uploaded by this hospital (hospitalId not null) */
    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.uploadedByHospitalId = :hospitalId " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC")
    List<VitalsRecord> findByGlobalPatientIdAndUploadedByHospitalIdOrderByRecordedAtDesc(
        @Param("patientId") Long patientId,
        @Param("hospitalId") Long hospitalId
    );

    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.uploadedByHospitalId = :hospitalId " +
           "AND vr.weightKg IS NOT NULL ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestWeightByPatientIdAndUploadedByHospitalId(
        @Param("patientId") Long patientId,
        @Param("hospitalId") Long hospitalId
    );

    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.uploadedByHospitalId = :hospitalId " +
           "AND vr.bloodGlucoseMgdl IS NOT NULL ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestBloodGlucoseByPatientIdAndUploadedByHospitalId(
        @Param("patientId") Long patientId,
        @Param("hospitalId") Long hospitalId
    );

    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.uploadedByHospitalId = :hospitalId " +
           "AND vr.bloodPressureSystolic IS NOT NULL AND vr.bloodPressureDiastolic IS NOT NULL " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC LIMIT 1")
    Optional<VitalsRecord> findLatestBloodPressureByPatientIdAndUploadedByHospitalId(
        @Param("patientId") Long patientId,
        @Param("hospitalId") Long hospitalId
    );

    @Query("SELECT vr FROM VitalsRecord vr WHERE vr.globalPatient.id = :patientId " +
           "AND vr.uploadedByHospitalId = :hospitalId " +
           "AND (:startDate IS NULL OR DATE(vr.recordedAt) >= :startDate) " +
           "AND (:endDate IS NULL OR DATE(vr.recordedAt) <= :endDate) " +
           "ORDER BY vr.recordedAt DESC, vr.createdAt DESC")
    List<VitalsRecord> findByPatientIdAndDateRangeAndUploadedByHospitalId(
        @Param("patientId") Long patientId,
        @Param("startDate") java.time.LocalDate startDate,
        @Param("endDate") java.time.LocalDate endDate,
        @Param("hospitalId") Long hospitalId
    );
}

