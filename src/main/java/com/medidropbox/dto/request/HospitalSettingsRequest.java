package com.medidropbox.dto.request;

import com.medidropbox.enums.BookingMode;
import com.medidropbox.enums.PendingPatientAction;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalSettingsRequest {
    @NotNull(message = "Booking mode is required")
    private BookingMode bookingMode = BookingMode.FUTURE_ALLOWED;
    
    @NotNull(message = "Future booking days is required")
    private Integer futureBookingDays = 0;
    
    private LocalTime bookingStartTime;
    
    private LocalTime bookingEndTime;
    
    @NotNull(message = "Daily patient limit is required")
    private Integer dailyPatientLimit = 30;
    
    @NotNull(message = "Online booking limit is required")
    private Integer onlineBookingLimit = 20;
    
    @NotNull(message = "Online booking enabled flag is required")
    private Boolean isOnlineBookingEnabled = true;
    
    @NotNull(message = "Booking allowed flag is required")
    private Boolean bookingAllowed = true;
    
    @NotNull(message = "Pending patient action is required")
    private PendingPatientAction pendingPatientAction = PendingPatientAction.CARRY_OVER;

    @NotNull(message = "Grace period days is required")
    @Min(value = 0, message = "Grace period cannot be negative")
    private Integer gracePeriodDays = 7;

    @NotNull(message = "Max recurring visits is required")
    @Min(value = 0, message = "Max recurring visits cannot be negative")
    private Integer maxRecurringVisits = 1;

    @NotNull(message = "Revisit queue interval is required")
    @Min(value = 1, message = "Revisit queue interval must be at least 1")
    private Integer revisitQueueInterval = 3;

    /** Optional URL to QR code image for payment (e.g. UPI QR). Shown on invoice when set. */
    private String paymentQrImageUrl;
}

