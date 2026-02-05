package com.medidropbox.service;

import com.medidropbox.dto.request.CreateStaffRequest;
import com.medidropbox.dto.response.StaffResponse;
import com.medidropbox.enums.Role;

import java.util.List;

public interface StaffManagementService {
    StaffResponse createStaff(Long hospitalId, CreateStaffRequest request);
    List<StaffResponse> getStaffByHospital(Long hospitalId);
    StaffResponse getStaffById(Long staffId, Long hospitalId);
    StaffResponse updateStaff(Long staffId, Long hospitalId, CreateStaffRequest request);
    void deactivateStaff(Long staffId, Long hospitalId);
    StaffResponse assignRoleToStaff(Long staffId, Long hospitalId, Role role);
}
