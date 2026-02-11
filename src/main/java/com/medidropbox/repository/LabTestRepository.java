package com.medidropbox.repository;

import com.medidropbox.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    List<LabTest> findByHospitalIdAndIsActiveTrueOrderByName(Long hospitalId);

    List<LabTest> findByHospitalIdOrderByName(Long hospitalId);

    Optional<LabTest> findByIdAndHospitalId(Long id, Long hospitalId);

    Optional<LabTest> findByHospitalIdAndCodeAndIdNot(Long hospitalId, String code, Long excludeId);

    Optional<LabTest> findByHospitalIdAndCode(Long hospitalId, String code);

    boolean existsByHospitalIdAndCode(Long hospitalId, String code);
}
