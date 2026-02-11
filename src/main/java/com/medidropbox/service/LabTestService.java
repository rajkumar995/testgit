package com.medidropbox.service;

import com.medidropbox.dto.request.LabTestRequest;
import com.medidropbox.dto.response.LabTestResponse;

import java.util.List;

public interface LabTestService {

    LabTestResponse create(Long hospitalId, LabTestRequest request);

    LabTestResponse update(Long hospitalId, Long labTestId, LabTestRequest request);

    LabTestResponse getById(Long hospitalId, Long labTestId);

    List<LabTestResponse> listByHospital(Long hospitalId, boolean activeOnly);

    void delete(Long hospitalId, Long labTestId);
}
