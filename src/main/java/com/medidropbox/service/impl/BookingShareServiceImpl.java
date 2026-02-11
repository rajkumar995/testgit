package com.medidropbox.service.impl;

import com.medidropbox.dto.request.BookingShareRequest;
import com.medidropbox.dto.response.BookingResponse;
import com.medidropbox.dto.response.BookingShareResponse;
import com.medidropbox.dto.response.ShareUrlResponse;
import com.medidropbox.entity.Booking;
import com.medidropbox.entity.BookingShare;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.HospitalPatient;
import com.medidropbox.entity.Payment;
import com.medidropbox.repository.BookingRepository;
import com.medidropbox.repository.BookingShareRepository;
import com.medidropbox.service.BookingShareService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingShareServiceImpl implements BookingShareService {
    
    private final BookingShareRepository bookingShareRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.share-view-base-url:}")
    private String shareViewBaseUrl;
    
    public BookingShareServiceImpl(BookingShareRepository bookingShareRepository,
                                  BookingRepository bookingRepository) {
        this.bookingShareRepository = bookingShareRepository;
        this.bookingRepository = bookingRepository;
    }
    
    @Override
    public ShareUrlResponse shareBooking(BookingShareRequest request, Long globalPatientId) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Verify patient owns the booking (check global patient)
        if (!booking.getGlobalPatient().getId().equals(globalPatientId)) {
            throw new RuntimeException("Unauthorized: Patient does not own this booking");
        }
        
        BookingShare share = new BookingShare();
        share.setBooking(booking);
        // Ensure expiresAt is in the future so the link works immediately after creation
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requestedExpiry = request.getExpiresAt();
        if (requestedExpiry == null || !requestedExpiry.isAfter(now)) {
            requestedExpiry = now.plusDays(7);
        }
        share.setExpiresAt(requestedExpiry);
        share.setIsActive(true);
        String shortCode = generateUniqueShortCode();
        share.setShortCode(shortCode);

        share = bookingShareRepository.save(share);
        
        // Verify the short code was saved correctly
        if (share.getShortCode() == null || !share.getShortCode().equals(shortCode)) {
            throw new RuntimeException("Failed to save short code");
        }

        String base = (shareViewBaseUrl != null && !shareViewBaseUrl.isBlank())
                ? shareViewBaseUrl.replaceAll("/$", "") : null;
        String shareUrl = base != null ? base + "/shared/" + shortCode : null;
        return ShareUrlResponse.builder().shareUrl(shareUrl).shortCode(shortCode).build();
    }

    @Override
    public List<BookingShareResponse> listMyShares(Long globalPatientId) {
        List<BookingShare> shares = bookingShareRepository.findByBooking_GlobalPatient_IdOrderByCreatedAtDesc(globalPatientId);
        return shares.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private String generateUniqueShortCode() {
        Random r = new Random();
        for (int i = 0; i < 20; i++) {
            String code = BookingShare.generateShortCode(r);
            if (bookingShareRepository.findByShortCode(code).isEmpty()) {
                return code;
            }
        }
        throw new RuntimeException("Unable to generate unique short code");
    }
    
    @Override
    public BookingShareResponse getSharedBookingByCodeOrToken(String codeOrToken) {
        String raw = codeOrToken != null ? codeOrToken.trim() : "";
        if (raw.isEmpty()) {
            throw new RuntimeException("Share link not found: Invalid code or token");
        }
        // Short code: 8 alphanumeric (e.g. Ab12Xy45). Token: UUID with dashes (36 chars).
        boolean isShortCode = raw.length() == 8 && !raw.contains("-");
        if (isShortCode) {
            return getSharedBookingByShortCodeInternal(raw);
        }
        return getSharedBookingByTokenInternal(raw);
    }

    private BookingShareResponse getSharedBookingByShortCodeInternal(String shortCode) {
        String normalized = shortCode.trim();
        Optional<BookingShare> anyShare = bookingShareRepository.findByShortCodeIgnoreCase(normalized);
        if (anyShare.isEmpty()) {
            throw new RuntimeException("Share link not found: Invalid short code");
        }
        BookingShare share = bookingShareRepository.findValidShortCode(normalized, LocalDateTime.now())
                .orElseThrow(() -> {
                    BookingShare found = anyShare.get();
                    if (!found.getIsActive()) {
                        return new RuntimeException("Share link has been revoked");
                    }
                    if (found.getExpiresAt() != null && found.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return new RuntimeException("Share link has expired");
                    }
                    return new RuntimeException("Share link is invalid");
                });
        return mapToResponse(share);
    }

    private BookingShareResponse getSharedBookingByTokenInternal(String shareToken) {
        BookingShare share = bookingShareRepository.findValidShareToken(shareToken, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired share token"));
        return mapToResponse(share);
    }
    
    @Override
    public void revokeShare(Long shareId, Long globalPatientId) {
        BookingShare share = bookingShareRepository.findById(shareId)
                .orElseThrow(() -> new RuntimeException("Share not found"));
        
        // Verify patient owns the booking (check global patient)
        if (!share.getBooking().getGlobalPatient().getId().equals(globalPatientId)) {
            throw new RuntimeException("Unauthorized: Patient does not own this booking");
        }
        
        share.setIsActive(false);
        bookingShareRepository.save(share);
    }
    
    private BookingShareResponse mapToResponse(BookingShare share) {
        String base = (shareViewBaseUrl != null && !shareViewBaseUrl.isBlank())
                ? shareViewBaseUrl.replaceAll("/$", "") : null;
        // Use shortCode in URL when present so /shared/FCZU78T9 works; fallback to shareToken (UUID)
        String pathSegment = (share.getShortCode() != null && !share.getShortCode().isBlank())
                ? share.getShortCode() : share.getShareToken();
        String shareUrl = base != null ? base + "/shared/" + pathSegment : null;

        Booking booking = share.getBooking();
        BookingResponse bookingResponse = mapBookingToResponse(booking);

        return BookingShareResponse.builder()
                .id(share.getId())
                .shareToken(share.getShareToken())
                .shareUrl(shareUrl)
                .expiresAt(share.getExpiresAt())
                .isActive(share.getIsActive())
                .booking(bookingResponse)
                .build();
    }

    private BookingResponse mapBookingToResponse(Booking booking) {
        BookingResponse.QueueInfo queueInfo = null;
        if (booking.getQueue() != null) {
            queueInfo = BookingResponse.QueueInfo.builder()
                    .id(booking.getQueue().getId())
                    .queueNumber(booking.getQueue().getQueueNumber())
                    .status(booking.getQueue().getStatus().name())
                    .build();
        }
        BookingResponse.PaymentInfo paymentInfo = null;
        if (booking.getPayment() != null) {
            Payment p = booking.getPayment();
            paymentInfo = BookingResponse.PaymentInfo.builder()
                    .id(p.getId())
                    .paymentMode(p.getPaymentMode().name())
                    .totalBill(p.getTotalBill())
                    .totalAmount(p.getTotalAmount())
                    .discount(p.getDiscount())
                    .taxableAmount(p.getTaxableAmount())
                    .gst(p.getGst())
                    .transactionId(p.getTransactionId())
                    .invoiceUrl(p.getInvoiceUrl())
                    .build();
        }
        HospitalPatient hp = booking.getHospitalPatient();
        GlobalPatient gp = booking.getGlobalPatient();
        return BookingResponse.builder()
                .id(booking.getId())
                .doctorId(booking.getDoctor().getId())
                .doctorName(booking.getDoctor().getName())
                .patientId(hp.getId())
                .patientName(hp.getFullName() != null ? hp.getFullName() : (gp != null ? gp.getFullName() : null))
                .patientPhone(hp.getPhone())
                .patientEmail(hp.getEmail() != null ? hp.getEmail() : (gp != null ? gp.getEmail() : null))
                .hospitalId(booking.getHospital().getId())
                .hospitalName(booking.getHospital().getName())
                .bookingDate(booking.getBookingDate())
                .bookingTime(booking.getBookingTime())
                .phoneNumber(booking.getPhoneNumber())
                .description(booking.getDescription())
                .symptoms(booking.getSymptoms())
                .chiefComplaint(booking.getChiefComplaint())
                .medicalHistory(booking.getMedicalHistory())
                .notes(booking.getNotes())
                .queue(queueInfo)
                .payment(paymentInfo)
                .status(booking.getStatus())
                .build();
    }
}
