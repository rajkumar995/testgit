package com.medidropbox.service;

import com.medidropbox.dto.request.DoctorFilterRequest;
import com.medidropbox.dto.response.DoctorResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.enums.Role;

import java.util.List;

public interface DoctorService {
    DoctorResponse getDoctorById(Long id, Role role);
    List<DoctorResponse> getDoctorsByHospital(Long hospitalId, Role role);
    PagedResponse<DoctorResponse> searchDoctors(DoctorFilterRequest filterRequest, Role role);
    DoctorResponse createDoctor(DoctorResponse request, Role role, Long userHospitalId);
    DoctorResponse updateDoctor(Long id, DoctorResponse request, Role role, Long currentUserId, Long userHospitalId);
    void deleteDoctor(Long id, Role role, Long userHospitalId);
}
