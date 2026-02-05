package com.medidropbox.service.impl;

import com.medidropbox.dto.request.QueueStatusUpdateRequest;
import com.medidropbox.dto.response.LiveQueueResponse;
import com.medidropbox.dto.response.QueueResponse;
import com.medidropbox.entity.Doctor;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.HospitalPatient;
import com.medidropbox.entity.Queue;
import com.medidropbox.enums.QueueStatus;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.DoctorRepository;
import com.medidropbox.repository.QueueRepository;
import com.medidropbox.service.QueueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queue Service Implementation
 * Handles queue status transitions and role-based filtering.
 * Stateless: no caches or long-lived state; all data from repository per request.
 */
@Service
@Transactional
public class QueueServiceImpl implements QueueService {

    private final QueueRepository queueRepository;
    private final DoctorRepository doctorRepository;

    public QueueServiceImpl(QueueRepository queueRepository, DoctorRepository doctorRepository) {
        this.queueRepository = queueRepository;
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LiveQueueResponse getLiveQueue(Long doctorId, LocalDate date, Role role, Long currentGlobalPatientId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        List<Queue> queues = queueRepository.findLiveQueueByDoctorAndDate(doctorId, date);
        
        // Get average consultation time (default to 15 minutes if not set)
        Integer avgConsultationTime = doctor.getAverageConsultationTime() != null 
                ? doctor.getAverageConsultationTime() 
                : 15;
        
        // Find current serving queue with details
        Queue currentServingQueue = queues.stream()
                .filter(q -> q.getStatus() == QueueStatus.SERVING)
                .findFirst()
                .orElse(null);
        
        Integer currentServingQueueNumber = currentServingQueue != null 
                ? currentServingQueue.getQueueNumber() 
                : null;
        
        // Build current serving patient info
        LiveQueueResponse.CurrentServingPatientInfo currentServingPatientInfo = null;
        if (currentServingQueue != null) {
            HospitalPatient hospitalPatient = currentServingQueue.getHospitalPatient();
            GlobalPatient globalPatient = currentServingQueue.getGlobalPatient();
            String patientName = hospitalPatient.getFullName() != null 
                    ? hospitalPatient.getFullName() 
                    : (globalPatient != null ? globalPatient.getFullName() : null);
            
            // Filter patient name based on role
            if (role == null || role == Role.PUBLIC) {
                patientName = null; // Hide name for public
            } else if (role == Role.PATIENT && currentGlobalPatientId != null 
                    && !currentServingQueue.getGlobalPatient().getId().equals(currentGlobalPatientId)) {
                patientName = null; // Hide other patients' names
            }
            
            currentServingPatientInfo = LiveQueueResponse.CurrentServingPatientInfo.builder()
                    .queueNumber(currentServingQueue.getQueueNumber())
                    .status(currentServingQueue.getStatus().name())
                    .patientName(patientName)
                    .build();
        }
        
        // Find patient's own queue
        Queue myQueue = null;
        if (currentGlobalPatientId != null) {
            myQueue = queues.stream()
                    .filter(q -> q.getGlobalPatient() != null 
                            && q.getGlobalPatient().getId().equals(currentGlobalPatientId))
                    .findFirst()
                    .orElse(null);
        }
        
        // Build my queue info with position-based wait time
        LiveQueueResponse.MyQueueInfo myQueueInfo = null;
        if (myQueue != null) {
            Integer myQueueNumber = myQueue.getQueueNumber();
            Integer position = null;
            Integer estimatedWaitTime = null;
            
            if (currentServingQueueNumber != null) {
                if (myQueue.getStatus() == QueueStatus.SERVING) {
                    position = 0;
                    estimatedWaitTime = 0;
                } else if (myQueue.getStatus() == QueueStatus.CALLED) {
                    position = 0;
                    estimatedWaitTime = 0; // You are next
                } else if (myQueueNumber > currentServingQueueNumber) {
                    // Calculate patients ahead
                    long patientsAhead = queues.stream()
                            .filter(q -> q.getQueueNumber() > currentServingQueueNumber 
                                    && q.getQueueNumber() < myQueueNumber
                                    && (q.getStatus() == QueueStatus.WAITING || q.getStatus() == QueueStatus.CALLED))
                            .count();
                    position = (int) patientsAhead;
                    estimatedWaitTime = position * avgConsultationTime;
                } else {
                    // Queue number is less than current serving (shouldn't happen normally)
                    position = null;
                    estimatedWaitTime = null;
                }
            } else {
                // No one is serving yet
                position = null;
                estimatedWaitTime = null;
            }
            
            myQueueInfo = LiveQueueResponse.MyQueueInfo.builder()
                    .queueNumber(myQueueNumber)
                    .status(myQueue.getStatus().name())
                    .position(position)
                    .estimatedWaitTime(estimatedWaitTime)
                    .build();
        }
        
        // Calculate waiting count
        long totalWaiting = queues.stream()
                .filter(q -> q.getStatus() == QueueStatus.WAITING || q.getStatus() == QueueStatus.CALLED)
                .count();
        
        // General estimate wait time (for all waiting patients)
        Integer generalEstimatedWaitTime = currentServingQueueNumber != null 
                ? (int) (totalWaiting * avgConsultationTime) 
                : null;
        
        // Only DOCTOR / STAFF / ADMIN see the full queue list; PATIENT and PUBLIC see only summary (no list)
        List<QueueResponse> queueResponses = (role != null && role != Role.PUBLIC && role != Role.PATIENT)
                ? queues.stream()
                        .map(q -> mapToResponse(q, currentServingQueueNumber, avgConsultationTime, queues))
                        .map(qr -> QueueResponse.filterByRole(qr, role, currentGlobalPatientId))
                        .collect(Collectors.toList())
                : Collections.emptyList();

        return LiveQueueResponse.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getName())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .bookingDate(date)
                .currentServingQueueNumber(currentServingQueueNumber)
                .totalWaiting((int) totalWaiting)
                .estimatedWaitTime(generalEstimatedWaitTime)
                .currentServingPatient(currentServingPatientInfo)
                .myQueue(myQueueInfo)
                .averageConsultationTime(avgConsultationTime)
                .queues(queueResponses)
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QueueResponse> getFullQueue(Long doctorId, LocalDate date, Role role, Long currentGlobalPatientId) {
        List<Queue> queues = queueRepository.findLiveQueueByDoctorAndDate(doctorId, date);
        
        return queues.stream()
                .map(this::mapToResponse)
                .map(qr -> QueueResponse.filterByRole(qr, role, currentGlobalPatientId))
                .sorted(Comparator.comparing(QueueResponse::getQueueNumber))
                .collect(Collectors.toList());
    }
    
    @Override
    public QueueResponse updateQueueStatus(Long queueId, QueueStatusUpdateRequest request, Role role) {
        // HOSPITAL_ADMIN, HOSPITAL_STAFF, ADMIN, SUPER_ADMIN can change queue status (must match controller @PreAuthorize)
        if (role != Role.HOSPITAL_ADMIN && role != Role.HOSPITAL_STAFF && role != Role.ADMIN && role != Role.SUPER_ADMIN) {
            throw new RuntimeException("Insufficient permissions to update queue status");
        }
        
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue not found"));
        
        // Validate status transition
        validateStatusTransition(queue.getStatus(), request.getStatus());
        
        queue.setStatus(request.getStatus());
        queue = queueRepository.save(queue);
        
        return mapToResponse(queue);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QueueResponse> getMyQueues(Long globalPatientId, Role role) {
        // Patient fetches ALL queues across ALL hospitals; each item includes current serving + status
        List<Queue> myQueues = queueRepository.findByGlobalPatientId(globalPatientId);

        return myQueues.stream()
                .map(q -> enrichMyQueueItem(q, globalPatientId, role))
                .collect(Collectors.toList());
    }

    /**
     * Build one "my queue" item with current serving info, position and estimated wait (all data already there).
     */
    private QueueResponse enrichMyQueueItem(Queue queue, Long globalPatientId, Role role) {
        Long doctorId = queue.getDoctor().getId();
        LocalDate date = queue.getBookingDate();
        List<Queue> queuesForDoctorDate = queueRepository.findLiveQueueByDoctorAndDate(doctorId, date);

        Queue currentServing = queuesForDoctorDate.stream()
                .filter(q -> q.getStatus() == QueueStatus.SERVING)
                .findFirst()
                .orElse(null);
        Integer currentServingQueueNumber = currentServing != null ? currentServing.getQueueNumber() : null;
        String currentServingStatus = currentServing != null ? currentServing.getStatus().name() : null;

        Integer avgConsultationTime = queue.getDoctor().getAverageConsultationTime() != null
                ? queue.getDoctor().getAverageConsultationTime()
                : 15;

        QueueResponse qr = mapToResponse(queue, currentServingQueueNumber, avgConsultationTime, queuesForDoctorDate);
        qr.setCurrentServingQueueNumber(currentServingQueueNumber);
        qr.setCurrentServingPatientStatus(currentServingStatus);
        return QueueResponse.filterByRole(qr, role, globalPatientId);
    }
    
    private void validateStatusTransition(QueueStatus current, QueueStatus next) {
        // Strict transition rules
        switch (current) {
            case WAITING:
                if (next != QueueStatus.CALLED && next != QueueStatus.CANCELLED && next != QueueStatus.NO_SHOW) {
                    throw new RuntimeException("Invalid status transition from WAITING");
                }
                break;
            case CALLED:
                if (next != QueueStatus.SERVING && next != QueueStatus.CANCELLED && next != QueueStatus.NO_SHOW) {
                    throw new RuntimeException("Invalid status transition from CALLED");
                }
                break;
            case SERVING:
                if (next != QueueStatus.SERVED && next != QueueStatus.CANCELLED) {
                    throw new RuntimeException("Invalid status transition from SERVING");
                }
                break;
            case SERVED:
            case CANCELLED:
            case NO_SHOW:
                throw new RuntimeException("Cannot change status from " + current);
            default:
                throw new RuntimeException("Unknown status: " + current);
        }
    }
    
    private QueueResponse mapToResponse(Queue queue) {
        return mapToResponse(queue, null, null, null);
    }
    
    private QueueResponse mapToResponse(Queue queue, Integer currentServingQueueNumber, 
                                       Integer avgConsultationTime, List<Queue> allQueues) {
        // Use hospital patient for display (hospital's record)
        HospitalPatient hospitalPatient = queue.getHospitalPatient();
        GlobalPatient globalPatient = queue.getGlobalPatient();
        
        // Calculate position and wait time for this specific queue
        Integer position = null;
        Integer estimatedWaitTime = null;
        
        if (currentServingQueueNumber != null && avgConsultationTime != null && allQueues != null) {
            Integer queueNumber = queue.getQueueNumber();
            
            if (queue.getStatus() == QueueStatus.SERVING) {
                position = 0;
                estimatedWaitTime = 0;
            } else if (queue.getStatus() == QueueStatus.CALLED) {
                position = 0;
                estimatedWaitTime = 0;
            } else if (queueNumber > currentServingQueueNumber 
                    && (queue.getStatus() == QueueStatus.WAITING || queue.getStatus() == QueueStatus.CALLED)) {
                // Calculate actual patients ahead by counting queues between current serving and this queue
                long patientsAhead = allQueues.stream()
                        .filter(q -> q.getQueueNumber() > currentServingQueueNumber 
                                && q.getQueueNumber() < queueNumber
                                && (q.getStatus() == QueueStatus.WAITING || q.getStatus() == QueueStatus.CALLED))
                        .count();
                position = (int) patientsAhead;
                estimatedWaitTime = position * avgConsultationTime;
            }
        }
        
        return QueueResponse.builder()
                .id(queue.getId())
                .bookingDate(queue.getBookingDate())
                .doctorId(queue.getDoctor().getId())
                .doctorName(queue.getDoctor().getName())
                .hospitalId(queue.getDoctor().getHospital() != null ? queue.getDoctor().getHospital().getId() : null)
                .hospitalName(queue.getDoctor().getHospital() != null ? queue.getDoctor().getHospital().getName() : null)
                .patientId(hospitalPatient.getId()) // Hospital patient ID
                .patientName(hospitalPatient.getFullName() != null ? hospitalPatient.getFullName() : 
                        (globalPatient != null ? globalPatient.getFullName() : null))
                .patientPhone(hospitalPatient.getPhone())
                .queueNumber(queue.getQueueNumber())
                .status(queue.getStatus())
                .bookingId(queue.getBooking().getId())
                .position(position)
                .estimatedWaitTime(estimatedWaitTime)
                .build();
    }
}
