package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Deletion Response
 * Response when patient requests data deletion
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletionResponse {
    
    private String status; // ARCHIVED, PERMANENTLY_DELETED, REJECTED
    private String message;
    private Boolean archived;
    private LocalDateTime archiveExpiresAt;
    private String reason;
    private String legalBasis;
    private LocalDateTime permanentDeletionDate;
    private List<Long> deletedFromHospitals;
    private String rejectionReason;
    private String rejectionCode; // LEGAL_HOLD, RETENTION_PERIOD, etc.
    
    public static DeletionResponse archived(String message, LocalDateTime expiresAt, 
                                           String reason, String legalBasis) {
        return DeletionResponse.builder()
            .status("ARCHIVED")
            .message(message)
            .archived(true)
            .archiveExpiresAt(expiresAt)
            .reason(reason)
            .legalBasis(legalBasis)
            .permanentDeletionDate(expiresAt)
            .build();
    }
    
    public static DeletionResponse permanentlyDeleted(String message, List<Long> deletedFromHospitals) {
        return DeletionResponse.builder()
            .status("PERMANENTLY_DELETED")
            .message(message)
            .archived(false)
            .deletedFromHospitals(deletedFromHospitals)
            .build();
    }
    
    public static DeletionResponse rejected(String message, String code, String reason) {
        return DeletionResponse.builder()
            .status("REJECTED")
            .message(message)
            .archived(false)
            .rejectionCode(code)
            .rejectionReason(reason)
            .build();
    }
}
