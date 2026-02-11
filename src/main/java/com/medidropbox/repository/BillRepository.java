package com.medidropbox.repository;

import com.medidropbox.entity.Bill;
import com.medidropbox.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByHospitalIdAndHospitalPatientIdOrderByCreatedAtDesc(Long hospitalId, Long hospitalPatientId);

    Page<Bill> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId, Pageable pageable);

    Optional<Bill> findByIdAndHospitalId(Long id, Long hospitalId);

    List<Bill> findByHospitalIdAndStatus(Long hospitalId, BillStatus status);
}
