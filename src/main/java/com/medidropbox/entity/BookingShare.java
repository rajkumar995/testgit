package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BookingShare entity - For sharing booking with friends/family.
 * shareToken: internal UUID. shortCode: short code for URL (e.g. /shared/Ab12Xy45).
 */
@Entity
@Table(name = "md_booking_shares", uniqueConstraints = {
    @UniqueConstraint(name = "UK_share_token", columnNames = {"share_token"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingShare extends BaseAuditEntity {

    private static final String SHORT_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0,O,1,I
    private static final int SHORT_CODE_LENGTH = 8;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Booking is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "share_token", unique = true, nullable = false, updatable = false)
    private String shareToken = UUID.randomUUID().toString();

    /** Short code for URL (e.g. shared/Ab12Xy45). Null for legacy rows. */
    @Column(name = "short_code", unique = true, length = 16, updatable = false)
    private String shortCode;

    @NotNull(message = "Expires at is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** Generate a unique-looking short code (caller should ensure uniqueness in DB). */
    public static String generateShortCode(java.util.Random random) {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(SHORT_CODE_CHARS.charAt(random.nextInt(SHORT_CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
