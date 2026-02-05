package com.medidropbox.service.impl;

import com.medidropbox.dto.response.ResolvePendingResponse;
import com.medidropbox.entity.Booking;
import com.medidropbox.entity.Queue;
import com.medidropbox.enums.BookingStatus;
import com.medidropbox.enums.PendingPatientAction;
import com.medidropbox.enums.QueueStatus;
import com.medidropbox.repository.BookingRepository;
import com.medidropbox.repository.QueueRepository;
import com.medidropbox.service.HospitalSettingsService;
import com.medidropbox.service.PendingBookingResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Resolves pending bookings for all dates before today (or before a given date).
 * Applies each hospital's pendingPatientAction setting. Reusable for API and cron.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PendingBookingResolutionServiceImpl implements PendingBookingResolutionService {

    private final QueueRepository queueRepository;
    private final BookingRepository bookingRepository;
    private final HospitalSettingsService hospitalSettingsService;

    @Override
    public ResolvePendingResponse resolvePendingBookingsForPastDays() {
        LocalDate today = LocalDate.now();
        return resolvePendingBookingsBefore(today);
    }

    @Override
    public ResolvePendingResponse resolvePendingBookingsForPastDays(Long hospitalId) {
        LocalDate today = LocalDate.now();
        return resolvePendingBookingsBefore(today, hospitalId);
    }

    @Override
    public ResolvePendingResponse resolvePendingBookingsBefore(LocalDate upToDateExclusive) {
        return resolvePendingBookingsBefore(upToDateExclusive, null);
    }

    @Override
    public ResolvePendingResponse resolvePendingBookingsBefore(LocalDate upToDateExclusive, Long hospitalId) {
        List<Queue> pending = hospitalId == null
                ? queueRepository.findPendingQueuesWithBookingDateBefore(upToDateExclusive)
                : queueRepository.findPendingQueuesWithBookingDateBeforeAndHospitalId(upToDateExclusive, hospitalId);

        int cancelledCount = 0;
        int carriedOverCount = 0;
        int manualSkippedCount = 0;

        for (Queue q : pending) {
            Long qHospitalId = q.getDoctor().getHospital().getId();
            PendingPatientAction action = hospitalSettingsService.getSettingsEntity(qHospitalId).getPendingPatientAction();

            if (action == PendingPatientAction.MANUAL) {
                manualSkippedCount++;
                continue;
            }

            if (action == PendingPatientAction.AUTO_CANCEL) {
                q.setStatus(QueueStatus.CANCELLED);
                queueRepository.save(q);
                Booking booking = q.getBooking();
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
                cancelledCount++;
                continue;
            }

            if (action == PendingPatientAction.CARRY_OVER) {
                boolean carriedOver = carryOverToNextDay(q);
                if (carriedOver) {
                    carriedOverCount++;
                } else {
                    cancelledCount++;
                }
            }
        }

        String message = String.format(
                "Resolved pending bookings before %s: %d cancelled, %d carried over, %d skipped (MANUAL). Total processed: %d.",
                upToDateExclusive, cancelledCount, carriedOverCount, manualSkippedCount, pending.size());

        return ResolvePendingResponse.builder()
                .resolvedUpToDateExclusive(upToDateExclusive)
                .cancelledCount(cancelledCount)
                .carriedOverCount(carriedOverCount)
                .manualSkippedCount(manualSkippedCount)
                .totalQueuesProcessed(pending.size())
                .message(message)
                .build();
    }

    /**
     * Carries over pending queue to next day (new booking + new queue). If patient already has
     * an active booking with same doctor on next date, cancels the old one instead (no duplicate).
     *
     * @return true if carried over, false if skipped (duplicate exists) and old one cancelled
     */
    private boolean carryOverToNextDay(Queue oldQueue) {
        Booking oldBooking = oldQueue.getBooking();
        LocalDate nextDate = oldBooking.getBookingDate().plusDays(1);

        Optional<Booking> existingOnNextDate = bookingRepository.findActiveBookingByPatientDoctorAndDate(
                oldBooking.getHospitalPatient(), oldBooking.getDoctor(), nextDate);
        if (existingOnNextDate.isPresent()) {
            oldQueue.setStatus(QueueStatus.CANCELLED);
            queueRepository.save(oldQueue);
            oldBooking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(oldBooking);
            return false;
        }

        Booking newBooking = new Booking();
        newBooking.setDoctor(oldBooking.getDoctor());
        newBooking.setHospitalPatient(oldBooking.getHospitalPatient());
        newBooking.setGlobalPatient(oldBooking.getGlobalPatient());
        newBooking.setHospital(oldBooking.getHospital());
        newBooking.setBookingDate(nextDate);
        newBooking.setBookingTime(oldBooking.getBookingTime());
        newBooking.setPhoneNumber(oldBooking.getPhoneNumber());
        newBooking.setDescription(oldBooking.getDescription());
        newBooking.setChiefComplaint(oldBooking.getChiefComplaint());
        newBooking.setMedicalHistory(oldBooking.getMedicalHistory());
        newBooking.setNotes(oldBooking.getNotes());
        newBooking.setStatus(BookingStatus.CONFIRMED);
        if (oldBooking.getSymptoms() != null) {
            newBooking.setSymptoms(new ArrayList<>(oldBooking.getSymptoms()));
        }

        newBooking = bookingRepository.save(newBooking);

        Integer maxQueue = queueRepository.findMaxQueueNumberByDoctorAndDate(oldQueue.getDoctor().getId(), nextDate);
        int nextQueueNumber = (maxQueue == null ? 1 : maxQueue + 1);

        Queue newQueue = new Queue();
        newQueue.setBookingDate(nextDate);
        newQueue.setDoctor(oldQueue.getDoctor());
        newQueue.setHospitalPatient(oldQueue.getHospitalPatient());
        newQueue.setGlobalPatient(oldQueue.getGlobalPatient());
        newQueue.setQueueNumber(nextQueueNumber);
        newQueue.setStatus(QueueStatus.WAITING);
        newQueue.setBooking(newBooking);
        queueRepository.save(newQueue);

        newBooking.setQueue(newQueue);
        bookingRepository.save(newBooking);

        oldQueue.setStatus(QueueStatus.CANCELLED);
        queueRepository.save(oldQueue);
        oldBooking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(oldBooking);
        return true;
    }
}
