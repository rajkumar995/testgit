package com.medidropbox.dto.response;

import com.medidropbox.enums.BookingMode;
import com.medidropbox.enums.PendingPatientAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalSettingsResponse {
    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private BookingMode bookingMode;
    private Integer futureBookingDays;
    private LocalTime bookingStartTime;
    private LocalTime bookingEndTime;
    private Integer dailyPatientLimit;
    private Integer onlineBookingLimit;
    private Boolean isOnlineBookingEnabled;
    private Boolean bookingAllowed;
    private PendingPatientAction pendingPatientAction;
    private Integer gracePeriodDays;
    private Integer maxRecurringVisits;
    private Integer revisitQueueInterval;
    /** Optional URL to QR code image for payment (e.g. UPI QR). */
    private String paymentQrImageUrl;
}

