package com.medidropbox.repository;

import com.medidropbox.entity.Doctor;
import com.medidropbox.entity.Queue;
import com.medidropbox.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueueRepository extends JpaRepository<Queue, Long> {
    List<Queue> findByDoctorAndBookingDate(Doctor doctor, LocalDate bookingDate);
    List<Queue> findByDoctorAndBookingDateAndStatus(Doctor doctor, LocalDate bookingDate, QueueStatus status);
    Optional<Queue> findByBookingId(Long bookingId);
    
    @Query("SELECT MAX(q.queueNumber) FROM Queue q WHERE q.doctor.id = :doctorId AND q.bookingDate = :date")
    Integer findMaxQueueNumberByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    @Query("SELECT q FROM Queue q WHERE q.doctor.id = :doctorId AND q.bookingDate = :date AND q.status = :status ORDER BY q.queueNumber")
    List<Queue> findByDoctorDateAndStatus(@Param("doctorId") Long doctorId, @Param("date") LocalDate date, @Param("status") QueueStatus status);
    
    @Query("SELECT q FROM Queue q WHERE q.doctor.id = :doctorId AND q.bookingDate = :date ORDER BY q.queueNumber")
    List<Queue> findLiveQueueByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);
    
    /**
     * Find all pending queues (WAITING or CALLED) with bookingDate strictly before the given date.
     * Used for end-of-day resolution: process all past days (e.g. before today).
     */
    @Query("SELECT q FROM Queue q WHERE q.bookingDate < :beforeDate AND q.status IN ('WAITING', 'CALLED') ORDER BY q.bookingDate, q.doctor.id, q.queueNumber")
    List<Queue> findPendingQueuesWithBookingDateBefore(@Param("beforeDate") java.time.LocalDate beforeDate);

    /**
     * Same as above but restricted to a single hospital (for HOSPITAL_ADMIN).
     */
    @Query("SELECT q FROM Queue q JOIN q.doctor d WHERE d.hospital.id = :hospitalId AND q.bookingDate < :beforeDate AND q.status IN ('WAITING', 'CALLED') ORDER BY q.bookingDate, q.doctor.id, q.queueNumber")
    List<Queue> findPendingQueuesWithBookingDateBeforeAndHospitalId(@Param("beforeDate") LocalDate beforeDate, @Param("hospitalId") Long hospitalId);

    // Cross-hospital patient queries (use globalPatient)
    @Query("SELECT q FROM Queue q WHERE q.globalPatient.id = :globalPatientId ORDER BY q.bookingDate DESC, q.queueNumber")
    List<Queue> findByGlobalPatientId(@Param("globalPatientId") Long globalPatientId);
    
    // Hospital-scoped queries (use hospitalPatient)
    @Query("SELECT q FROM Queue q WHERE q.hospitalPatient.id = :hospitalPatientId ORDER BY q.bookingDate DESC, q.queueNumber")
    List<Queue> findByHospitalPatientId(@Param("hospitalPatientId") Long hospitalPatientId);
}
