package com.medidropbox.scheduler;

import com.medidropbox.dto.response.ResolvePendingResponse;
import com.medidropbox.service.PendingBookingResolutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to resolve pending bookings for past days (auto-cancel or carry-over)
 * based on each hospital's pendingPatientAction setting.
 * Runs daily at configurable time (default: midnight). Disable with app.scheduler.pending-bookings.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "app.scheduler.pending-bookings.enabled", havingValue = "true", matchIfMissing = true)
public class PendingBookingResolutionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PendingBookingResolutionScheduler.class);

    private final PendingBookingResolutionService pendingBookingResolutionService;

    public PendingBookingResolutionScheduler(PendingBookingResolutionService pendingBookingResolutionService) {
        this.pendingBookingResolutionService = pendingBookingResolutionService;
    }

    /**
     * Process all pending queues (WAITING/CALLED) with bookingDate before today.
     * Applies each hospital's setting: AUTO_CANCEL, CARRY_OVER, or MANUAL (skip).
     */
    @Scheduled(cron = "${app.scheduler.pending-bookings.cron:0 0 0 * * ?}")
    public void resolvePendingBookingsDaily() {
        log.info("Scheduled job: resolving pending bookings for past days");
        try {
            ResolvePendingResponse response = pendingBookingResolutionService.resolvePendingBookingsForPastDays();
            log.info("Pending bookings resolution completed: cancelled={}, carriedOver={}, manualSkipped={}",
                    response.getCancelledCount(), response.getCarriedOverCount(), response.getManualSkippedCount());
        } catch (Exception e) {
            log.error("Scheduled pending bookings resolution failed", e);
        }
    }
}
