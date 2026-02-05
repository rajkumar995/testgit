package com.medidropbox.controller;

import com.medidropbox.dto.response.BookingShareResponse;
import com.medidropbox.dto.response.HospitalResponse;
import com.medidropbox.dto.response.LiveQueueResponse;
import com.medidropbox.enums.Role;
import com.medidropbox.service.BookingShareService;
import com.medidropbox.service.HospitalService;
import com.medidropbox.service.QueueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Serves the shared booking page (Thymeleaf).
 * When backend JAR is running, /shared/{token} returns HTML so the friend can view the booking without a separate frontend.
 * Includes live doctor queue info, booking times, and hospital details (address, phone, services, map link).
 */
@Controller
public class SharedBookingViewController {

    private final BookingShareService bookingShareService;
    private final QueueService queueService;
    private final HospitalService hospitalService;
    private final String appDownloadUrl;

    public SharedBookingViewController(BookingShareService bookingShareService, QueueService queueService,
                                       HospitalService hospitalService,
                                       @Value("${app.share.app-download-url:}") String appDownloadUrl) {
        this.bookingShareService = bookingShareService;
        this.queueService = queueService;
        this.hospitalService = hospitalService;
        this.appDownloadUrl = (appDownloadUrl != null && !appDownloadUrl.isBlank()) ? appDownloadUrl : null;
    }

    @GetMapping("/shared/{codeOrToken}")
    public String viewSharedBooking(@PathVariable String codeOrToken, Model model) {
        try {
            String raw = codeOrToken != null ? codeOrToken.trim() : "";
            // Short code: 8 alphanumeric (e.g. Ab12Xy45). Token: UUID with dashes (36 chars).
            boolean isShortCode = raw.length() == 8 && !raw.contains("-");
            BookingShareResponse data;
            if (isShortCode) {
                data = bookingShareService.getSharedBookingByShortCode(raw);
            } else {
                // Token (UUID) or legacy link — try token first
                data = bookingShareService.getSharedBooking(raw);
            }
            model.addAttribute("share", data);
            model.addAttribute("booking", data.getBooking());

            // Live queue for this doctor/date (public view – no patient identity)
            LiveQueueResponse liveQueue = null;
            if (data.getBooking() != null
                    && data.getBooking().getDoctorId() != null
                    && data.getBooking().getBookingDate() != null
                    && !data.getBooking().getBookingDate().isBefore(LocalDate.now())) {
                try {
                    liveQueue = queueService.getLiveQueue(
                            data.getBooking().getDoctorId(),
                            data.getBooking().getBookingDate(),
                            Role.PUBLIC,
                            null);
                } catch (Exception ignored) {
                    // Keep liveQueue null if fetch fails (e.g. doctor/queue not found)
                }
            }
            model.addAttribute("liveQueue", liveQueue);

            // Hospital details for shared page (address, phone, services, map)
            HospitalResponse hospitalDetails = null;
            if (data.getBooking() != null && data.getBooking().getHospitalId() != null) {
                try {
                    hospitalDetails = hospitalService.getHospitalByIdForSharedView(data.getBooking().getHospitalId());
                } catch (Exception ignored) {
                    // Keep null if hospital not found
                }
            }
            model.addAttribute("hospitalDetails", hospitalDetails);
            model.addAttribute("appDownloadUrl", appDownloadUrl);
            if (data.getBooking() != null) {
                if (data.getBooking().getBookingDate() != null) {
                    model.addAttribute("bookingDateFormatted",
                        data.getBooking().getBookingDate().format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)));
                }
                if (data.getBooking().getBookingTime() != null) {
                    model.addAttribute("bookingTimeFormatted",
                        data.getBooking().getBookingTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)));
                }
            }

            return "shared-booking";
        } catch (Exception e) {
            model.addAttribute("message", "This link has expired or been revoked. Please ask your friend to share the booking with you again.");
            return "shared-booking-expired";
        }
    }
}
