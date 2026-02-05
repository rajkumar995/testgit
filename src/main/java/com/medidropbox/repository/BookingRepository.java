package com.medidropbox.repository;

import com.medidropbox.entity.Booking;
import com.medidropbox.entity.Doctor;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.HospitalPatient;
import com.medidropbox.enums.BookingStatus;
import com.medidropbox.enums.QueueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Hospital-scoped queries (use hospitalPatient)
    List<Booking> findByHospitalPatient(HospitalPatient hospitalPatient);
    List<Booking> findByHospitalPatientAndStatus(HospitalPatient hospitalPatient, BookingStatus status);
    List<Booking> findByHospital(Hospital hospital);
    List<Booking> findByHospitalId(Long hospitalId);
    
    // Doctor queries
    List<Booking> findByDoctor(Doctor doctor);
    List<Booking> findByDoctorAndBookingDate(Doctor doctor, LocalDate bookingDate);
    List<Booking> findByHospitalAndBookingDate(Hospital hospital, LocalDate bookingDate);
    
    // Cross-hospital patient queries (use globalPatient)
    List<Booking> findByGlobalPatient(GlobalPatient globalPatient);
    List<Booking> findByGlobalPatientId(Long globalPatientId);
    
    @Query("SELECT b FROM Booking b WHERE b.globalPatient.id = :globalPatientId ORDER BY b.bookingDate DESC, b.bookingTime DESC")
    Page<Booking> findByGlobalPatientIdOrderByDateDesc(@Param("globalPatientId") Long globalPatientId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.hospital.id = :hospitalId AND b.bookingDate = :date ORDER BY b.bookingTime")
    List<Booking> findByHospitalAndDate(@Param("hospitalId") Long hospitalId, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.hospitalPatient.hospital.id = :hospitalId AND b.hospitalPatient.id = :hospitalPatientId")
    List<Booking> findByHospitalAndHospitalPatient(@Param("hospitalId") Long hospitalId, @Param("hospitalPatientId") Long hospitalPatientId);
    
    /**
     * Find existing booking for same patient, doctor, and date with active queue
     * Active queue means: queue status is WAITING, CALLED, or SERVING (not CANCELLED or SERVED)
     * Returns booking if it exists and is not cancelled, and queue is still active
     */
    @Query("SELECT b FROM Booking b WHERE b.hospitalPatient = :hospitalPatient " +
           "AND b.doctor = :doctor AND b.bookingDate = :bookingDate " +
           "AND b.status != com.medidropbox.enums.BookingStatus.CANCELLED " +
           "AND (b.queue IS NULL OR b.queue.status NOT IN (com.medidropbox.enums.QueueStatus.CANCELLED, com.medidropbox.enums.QueueStatus.SERVED))")
    Optional<Booking> findActiveBookingByPatientDoctorAndDate(
            @Param("hospitalPatient") HospitalPatient hospitalPatient,
            @Param("doctor") Doctor doctor,
            @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * Count total bookings for a doctor on a specific date (all bookings including staff and patient)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.doctor.id = :doctorId " +
           "AND b.bookingDate = :bookingDate AND b.status != com.medidropbox.enums.BookingStatus.CANCELLED")
    Long countBookingsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("bookingDate") LocalDate bookingDate);
    
    /**
     * Count online patient bookings for a doctor on a specific date
     * Online booking = booking created by patient (has globalPatient linked)
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.doctor.id = :doctorId " +
           "AND b.bookingDate = :bookingDate AND b.status != com.medidropbox.enums.BookingStatus.CANCELLED " +
           "AND b.globalPatient IS NOT NULL")
    Long countOnlineBookingsByDoctorAndDate(@Param("doctorId") Long doctorId, @Param("bookingDate") LocalDate bookingDate);
}
