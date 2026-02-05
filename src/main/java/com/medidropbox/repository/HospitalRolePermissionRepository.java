package com.medidropbox.repository;

import com.medidropbox.entity.HospitalRolePermission;
import com.medidropbox.enums.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRolePermissionRepository extends JpaRepository<HospitalRolePermission, Long> {
    List<HospitalRolePermission> findByHospitalRoleId(Long hospitalRoleId);
    boolean existsByHospitalRoleIdAndPermission(Long hospitalRoleId, Permission permission);
    void deleteByHospitalRoleIdAndPermission(Long hospitalRoleId, Permission permission);
    void deleteByHospitalRoleId(Long hospitalRoleId);
}

