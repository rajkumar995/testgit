package com.medidropbox.service;

import com.medidropbox.dto.request.HospitalFilterRequest;
import com.medidropbox.dto.request.HospitalRegistrationRequest;
import com.medidropbox.dto.response.HospitalResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.enums.Role;

import java.util.List;

public interface HospitalService {
    HospitalResponse registerHospital(HospitalRegistrationRequest request);
    HospitalResponse getHospitalById(Long id, Role role);
    /** Hospital details for shared booking page: address, phone, services, map link (phone included for shared view). */
    HospitalResponse getHospitalByIdForSharedView(Long id);
    List<HospitalResponse> getAllHospitals(Role role);
    PagedResponse<HospitalResponse> searchHospitals(HospitalFilterRequest filterRequest, Role role);
    HospitalResponse updateHospital(Long id, HospitalRegistrationRequest request, Role role);
    void deactivateHospital(Long id, Role role);
}
