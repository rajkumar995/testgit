package com.medidropbox.service;

import com.medidropbox.dto.request.QueueStatusUpdateRequest;
import com.medidropbox.dto.response.LiveQueueResponse;
import com.medidropbox.dto.response.QueueResponse;
import com.medidropbox.enums.Role;

import java.time.LocalDate;
import java.util.List;

public interface QueueService {
    LiveQueueResponse getLiveQueue(Long doctorId, LocalDate date, Role role, Long currentGlobalPatientId);
    List<QueueResponse> getFullQueue(Long doctorId, LocalDate date, Role role, Long currentGlobalPatientId);
    QueueResponse updateQueueStatus(Long queueId, QueueStatusUpdateRequest request, Role role);
    List<QueueResponse> getMyQueues(Long globalPatientId, Role role);
}
