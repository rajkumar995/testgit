package com.medidropbox.repository;

import com.medidropbox.entity.HospitalRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRoleRepository extends JpaRepository<HospitalRole, Long> {
    List<HospitalRole> findByHospitalIdAndIsActiveTrue(Long hospitalId);
    Optional<HospitalRole> findByHospitalIdAndRoleName(Long hospitalId, String roleName);
    boolean existsByHospitalIdAndRoleName(Long hospitalId, String roleName);
    Optional<HospitalRole> findByIdAndHospitalId(Long id, Long hospitalId);
}

