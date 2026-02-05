package com.medidropbox.service.impl;

import com.medidropbox.dto.request.BookingRequest;
import com.medidropbox.dto.request.PaymentRequest;
import com.medidropbox.dto.response.BookingResponse;
import com.medidropbox.entity.*;
import com.medidropbox.enums.BookingStatus;
import com.medidropbox.enums.QueueStatus;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.*;
import com.medidropbox.enums.BookingMode;
import com.medidropbox.service.AuditLogService;
import com.medidropbox.service.AwsS3Service;
import com.medidropbox.service.BookingService;
import com.medidropbox.service.HospitalPatientService;
import com.medidropbox.service.HospitalSettingsService;
import com.medidropbox.service.InvoicePdfService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Booking Service Implementation
 * Core rule: When booking is created, Queue is automatically created
 * Supports both hospital-scoped and cross-hospital patient queries
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final QueueRepository queueRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalPatientRepository hospitalPatientRepository;
    private final GlobalPatientRepository globalPatientRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalPatientService hospitalPatientService;
    private final PaymentRepository paymentRepository;
    private final HospitalSettingsService hospitalSettingsService;
    private final AuditLogService auditLogService;
    private final InvoicePdfService invoicePdfService;
    private final AwsS3Service awsS3Service;

    public BookingServiceImpl(BookingRepository bookingRepository,
                            QueueRepository queueRepository,
                            DoctorRepository doctorRepository,
                            HospitalPatientRepository hospitalPatientRepository,
                            GlobalPatientRepository globalPatientRepository,
                            HospitalRepository hospitalRepository,
                            HospitalPatientService hospitalPatientService,
                            PaymentRepository paymentRepository,
                            HospitalSettingsService hospitalSettingsService,
                            AuditLogService auditLogService,
                            InvoicePdfService invoicePdfService,
                            AwsS3Service awsS3Service) {
        this.bookingRepository = bookingRepository;
        this.queueRepository = queueRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalPatientRepository = hospitalPatientRepository;
        this.globalPatientRepository = globalPatientRepository;
        this.hospitalRepository = hospitalRepository;
        this.hospitalPatientService = hospitalPatientService;
        this.paymentRepository = paymentRepository;
        this.hospitalSettingsService = hospitalSettingsService;
        this.auditLogService = auditLogService;
        this.invoicePdfService = invoicePdfService;
        this.awsS3Service = awsS3Service;
    }

    @Override
    public BookingResponse createBooking(BookingRequest request, Long globalPatientId, Long createdByUserId) {
        // Fetch entities
        Doctor doctor = doctorRepository.findByIdAndIsActiveTrue(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found or inactive"));
        
        Hospital hospital = hospitalRepository.findByIdAndIsActiveTrue(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found or inactive"));
        
        // Validate doctor belongs to hospital
        if (!doctor.getHospital().getId().equals(hospital.getId())) {
            throw new RuntimeException("Doctor does not belong to the specified hospital");
        }
        
        // Get hospital settings and validate booking rules
        com.medidropbox.entity.HospitalSettings settings = hospitalSettingsService.getSettingsEntity(hospital.getId());
        
        // Determine if this is a patient booking (has globalPatientId) or staff booking
        boolean isPatientBooking = globalPatientId != null;
        
        // Validate hospital settings for patient bookings
        if (isPatientBooking) {
            // Check if online booking is enabled
            if (!settings.getIsOnlineBookingEnabled()) {
                throw new RuntimeException("Online booking is currently disabled for this hospital. Please contact the hospital directly.");
            }
            
            // Check if booking is allowed
            if (!settings.getBookingAllowed()) {
                throw new RuntimeException("Online booking is currently closed for this hospital. Please try again later or contact the hospital directly.");
            }
        }
        
        // Validate booking date based on booking mode
        LocalDate today = LocalDate.now();
        LocalDate bookingDate = request.getBookingDate();
        
        if (settings.getBookingMode() == BookingMode.SAME_DAY) {
            // Only allow today's bookings
            if (!bookingDate.equals(today)) {
                throw new RuntimeException(
                        String.format("This hospital only allows same-day bookings. Please select today's date (%s).", today));
            }
        } else if (settings.getBookingMode() == BookingMode.FUTURE_ALLOWED) {
            // Allow future bookings up to future_booking_days
            if (bookingDate.isBefore(today)) {
                throw new RuntimeException("Booking date cannot be in the past.");
            }
            
            long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(today, bookingDate);
            if (daysDifference > settings.getFutureBookingDays()) {
                throw new RuntimeException(
                        String.format("This hospital allows bookings up to %d days in advance. Please select a date within the allowed range.",
                                settings.getFutureBookingDays()));
            }
        }
        
        // Check daily patient limit per doctor
        Long totalBookings = bookingRepository.countBookingsByDoctorAndDate(doctor.getId(), bookingDate);
        if (totalBookings >= settings.getDailyPatientLimit()) {
            throw new RuntimeException(
                    String.format("Daily booking limit (%d) has been reached for Dr. %s on %s. Please select another date or contact the hospital.",
                            settings.getDailyPatientLimit(), doctor.getName(), bookingDate));
        }
        
        // Check online booking limit per doctor (only for patient bookings)
        if (isPatientBooking) {
            Long onlineBookings = bookingRepository.countOnlineBookingsByDoctorAndDate(doctor.getId(), bookingDate);
            if (onlineBookings >= settings.getOnlineBookingLimit()) {
                throw new RuntimeException(
                        String.format("Online booking limit (%d) has been reached for Dr. %s on %s. Please select another date or contact the hospital.",
                                settings.getOnlineBookingLimit(), doctor.getName(), bookingDate));
            }
        }
        
        // Detect "book for friend" (another person): bookForPhone present and different from logged-in user's phone
        String bookForPhone = request.getBookForPhone() != null ? request.getBookForPhone().trim() : null;
        boolean isBookForFriend = false;
        if (bookForPhone != null && !bookForPhone.isBlank() && globalPatientId != null) {
            String currentUserPhone = globalPatientRepository.findById(globalPatientId)
                    .map(GlobalPatient::getPhone)
                    .orElse(null);
            if (currentUserPhone != null && !currentUserPhone.trim().equals(bookForPhone)) {
                isBookForFriend = true;
            }
        }

        if (isBookForFriend) {
            if (!Boolean.TRUE.equals(request.getBookOnBehalfConsent())) {
                throw new RuntimeException("Consent required: You must confirm that you have consent to book on behalf of this person (bookOnBehalfConsent: true).");
            }
            if (request.getBookForName() == null || request.getBookForName().isBlank()) {
                throw new RuntimeException("Friend's name is required when booking for another person (bookForName).");
            }
        }

        // Get or create hospital patient for this booking
        String phone;
        HospitalPatient hospitalPatient;
        GlobalPatient globalPatient;

        if (isBookForFriend) {
            hospitalPatient = hospitalPatientService.createOrGetHospitalPatientForBookForFriend(
                    hospital.getId(),
                    request.getBookForName(),
                    bookForPhone,
                    request.getBookForDateOfBirth(),
                    request.getBookForEmail(),
                    request.getBookForDescription());
            phone = bookForPhone;
            globalPatient = globalPatientRepository.findById(globalPatientId)
                    .orElseThrow(() -> new RuntimeException("Global patient not found"));
        } else {
            phone = request.getPhoneNumber();
            if (phone == null && globalPatientId != null) {
                GlobalPatient gp = globalPatientRepository.findById(globalPatientId)
                        .orElseThrow(() -> new RuntimeException("Global patient not found"));
                phone = gp.getPhone();
            }
            if (phone == null) {
                throw new RuntimeException("Phone number is required for booking");
            }
            Boolean dataConsent = request.getDataConsent() != null ? request.getDataConsent() : false;
            hospitalPatient = hospitalPatientService.createOrGetHospitalPatient(
                    hospital.getId(), phone, null, null, dataConsent);
            if (globalPatientId != null) {
                globalPatient = globalPatientRepository.findById(globalPatientId)
                        .orElseThrow(() -> new RuntimeException("Global patient not found"));
                if (!globalPatient.getPhone().equals(phone)) {
                    throw new RuntimeException("Phone number mismatch");
                }
            } else {
                globalPatient = hospitalPatient.getGlobalPatient();
            }
        }
        
        // Check for duplicate booking: same patient, same doctor, same date with active queue
        Optional<Booking> existingBooking = bookingRepository.findActiveBookingByPatientDoctorAndDate(
                hospitalPatient, doctor, request.getBookingDate());
        
        if (existingBooking.isPresent()) {
            Booking existing = existingBooking.get();
            String queueStatus = existing.getQueue() != null 
                    ? existing.getQueue().getStatus().name() 
                    : "N/A";
            throw new RuntimeException(
                    String.format("A booking already exists for this patient with Dr. %s on %s. " +
                            "Queue status: %s. Please cancel the existing booking or wait for it to be completed before creating a new one.",
                            doctor.getName(), request.getBookingDate(), queueStatus));
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setDoctor(doctor);
        booking.setHospitalPatient(hospitalPatient);
        booking.setGlobalPatient(globalPatient);
        booking.setHospital(hospital);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setPhoneNumber(phone);
        booking.setDescription(request.getDescription());
        booking.setSymptoms(request.getSymptoms());
        booking.setChiefComplaint(request.getChiefComplaint());
        booking.setMedicalHistory(request.getMedicalHistory());
        booking.setNotes(request.getNotes());
        booking.setStatus(BookingStatus.CONFIRMED);
        
        booking = bookingRepository.save(booking);

        if (isBookForFriend && createdByUserId != null) {
            auditLogService.logDataModification(createdByUserId, "Booking", booking.getId(),
                    "BOOKING_FOR_ANOTHER_PERSON",
                    String.format("ownerGlobalPatientId=%d hospitalPatientId=%d (booked for friend)", globalPatientId, hospitalPatient.getId()));
        }
        
        // Create payment if provided in request
        if (request.getPayment() != null) {
            Payment payment = createPaymentFromRequest(request.getPayment());
            payment = paymentRepository.save(payment);
            booking.setPayment(payment);
            booking = bookingRepository.save(booking);
        }
        
        // Automatically create queue
        Queue queue = new Queue();
        queue.setBookingDate(request.getBookingDate());
        queue.setDoctor(doctor);
        queue.setHospitalPatient(hospitalPatient);
        queue.setGlobalPatient(globalPatient);
        queue.setBooking(booking);
        queue.setStatus(QueueStatus.WAITING);
        
        // Assign next queue number (unique per doctor + date)
        Integer nextQueueNumber = queueRepository.findMaxQueueNumberByDoctorAndDate(
                doctor.getId(), request.getBookingDate());
        queue.setQueueNumber(nextQueueNumber == null ? 1 : nextQueueNumber + 1);
        
        queue = queueRepository.save(queue);
        
        // Update booking with queue
        booking.setQueue(queue);
        booking = bookingRepository.save(booking);
        
        // Generate invoice PDF, upload to S3, set URL in payment (patient or hospital booking)
        if (booking.getPayment() != null) {
            try {
                byte[] pdfBytes = invoicePdfService.generateInvoicePdf(booking);
                String fileName = "invoice-booking-" + booking.getId() + "-" + UUID.randomUUID().toString().substring(0, 8) + ".pdf";
                String invoiceUrl = awsS3Service.uploadFile(pdfBytes, fileName, "invoices/", "application/pdf");
                Payment payment = booking.getPayment();
                payment.setInvoiceUrl(invoiceUrl);
                paymentRepository.save(payment);
            } catch (Exception e) {
                // Log but do not fail booking; invoice can be regenerated later if needed
                org.slf4j.LoggerFactory.getLogger(BookingServiceImpl.class)
                        .warn("Invoice generation/upload failed for booking {}: {}", booking.getId(), e.getMessage());
            }
        }
        
        return mapToResponse(booking, Role.PATIENT, globalPatientId, globalPatientId);
    }
    
    @Override
    public List<BookingResponse> getMyBookings(Long globalPatientId, Role role) {
        // Patient fetches ALL bookings across ALL hospitals using globalPatientId
        GlobalPatient globalPatient = globalPatientRepository.findById(globalPatientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        List<Booking> bookings = bookingRepository.findByGlobalPatient(globalPatient);
        return bookings.stream()
                .map(b -> mapToResponse(b, role, null, globalPatientId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getBookingsByHospital(Long hospitalId, Role role) {
        // Hospital fetches bookings using hospital-scoped query
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        List<Booking> bookings = bookingRepository.findByHospital(hospital);
        return bookings.stream()
                .map(b -> mapToResponse(b, role, null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<BookingResponse> getBookingsByDoctor(Long doctorId, Role role) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        List<Booking> bookings = bookingRepository.findByDoctor(doctor);
        return bookings.stream()
                .map(b -> mapToResponse(b, role, null, null))
                .collect(Collectors.toList());
    }
    
    @Override
    public BookingResponse getBookingById(Long id, Role role, Long currentUserId, Long currentPatientId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        return mapToResponse(booking, role, currentUserId, currentPatientId);
    }
    
    private BookingResponse mapToResponse(Booking booking, Role role, Long currentUserId, Long currentPatientId) {
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
            Payment payment = booking.getPayment();
            paymentInfo = BookingResponse.PaymentInfo.builder()
                    .id(payment.getId())
                    .paymentMode(payment.getPaymentMode().name())
                    .totalBill(payment.getTotalBill())
                    .totalAmount(payment.getTotalAmount())
                    .discount(payment.getDiscount())
                    .taxableAmount(payment.getTaxableAmount())
                    .gst(payment.getGst())
                    .transactionId(payment.getTransactionId())
                    .invoiceUrl(payment.getInvoiceUrl())
                    .build();
        }
        
        // Use hospital patient for display (hospital's record)
        HospitalPatient hospitalPatient = booking.getHospitalPatient();
        GlobalPatient globalPatient = booking.getGlobalPatient();
        
        BookingResponse response = BookingResponse.builder()
                .id(booking.getId())
                .doctorId(booking.getDoctor().getId())
                .doctorName(booking.getDoctor().getName())
                .patientId(hospitalPatient.getId()) // Hospital patient ID for hospital view
                .patientName(hospitalPatient.getFullName() != null ? hospitalPatient.getFullName() : globalPatient.getFullName())
                .patientPhone(hospitalPatient.getPhone())
                .patientEmail(hospitalPatient.getEmail() != null ? hospitalPatient.getEmail() : globalPatient.getEmail())
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
        
        return BookingResponse.filterByRole(response, role, currentUserId, currentPatientId);
    }
    
    /**
     * Create Payment entity from PaymentRequest
     */
    private Payment createPaymentFromRequest(PaymentRequest paymentRequest) {
        Payment payment = new Payment();
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMode(paymentRequest.getPaymentMode());
        payment.setTotalBill(paymentRequest.getTotalBill());
        payment.setTotalAmount(paymentRequest.getTotalAmount());
        payment.setDiscount(paymentRequest.getDiscount() != null ? paymentRequest.getDiscount() : BigDecimal.ZERO);
        payment.setTaxableAmount(paymentRequest.getTaxableAmount());
        payment.setGst(paymentRequest.getGst());
        payment.setTransactionId(paymentRequest.getTransactionId());
        return payment;
    }
}
