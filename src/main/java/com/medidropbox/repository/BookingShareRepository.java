package com.medidropbox.repository;

import com.medidropbox.entity.BookingShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingShareRepository extends JpaRepository<BookingShare, Long> {
    Optional<BookingShare> findByShareToken(String shareToken);
    Optional<BookingShare> findByShareTokenAndIsActiveTrue(String shareToken);
    Optional<BookingShare> findByShortCode(String shortCode);

    /** Case-insensitive short code lookup (URLs may be lowercased). Caller should pass trimmed code. */
    @Query("SELECT bs FROM BookingShare bs WHERE LOWER(bs.shortCode) = LOWER(:code)")
    Optional<BookingShare> findByShortCodeIgnoreCase(@Param("code") String code);

    @Query("SELECT bs FROM BookingShare bs WHERE bs.shareToken = :token AND bs.isActive = true AND (bs.expiresAt IS NULL OR bs.expiresAt > :now)")
    Optional<BookingShare> findValidShareToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /** Case-insensitive so /shared/abc12xy4 matches stored Ab12Xy45. Caller should pass trimmed code. */
    @Query("SELECT bs FROM BookingShare bs WHERE LOWER(bs.shortCode) = LOWER(:code) AND bs.isActive = true AND (bs.expiresAt IS NULL OR bs.expiresAt > :now)")
    Optional<BookingShare> findValidShortCode(@Param("code") String code, @Param("now") LocalDateTime now);

    List<BookingShare> findByBookingId(Long bookingId);

    /** All shares created by this patient (for "my shared queues" list). */
    @Query("SELECT bs FROM BookingShare bs WHERE bs.booking.globalPatient.id = :globalPatientId ORDER BY bs.createdAt DESC")
    List<BookingShare> findByBooking_GlobalPatient_IdOrderByCreatedAtDesc(@Param("globalPatientId") Long globalPatientId);
}
