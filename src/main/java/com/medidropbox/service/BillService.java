package com.medidropbox.service;

import com.medidropbox.dto.request.BillRequest;
import com.medidropbox.dto.response.BillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillService {

    BillResponse create(Long hospitalId, BillRequest request);

    BillResponse update(Long hospitalId, Long billId, BillRequest request);

    BillResponse getById(Long hospitalId, Long billId);

    BillResponse completeBill(Long hospitalId, Long billId);

    Page<BillResponse> listByHospital(Long hospitalId, Pageable pageable);

    List<BillResponse> listByHospitalAndPatient(Long hospitalId, Long hospitalPatientId);
}
