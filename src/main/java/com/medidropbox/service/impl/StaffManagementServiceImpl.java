package com.medidropbox.service.impl;

import com.medidropbox.dto.request.CreateStaffRequest;
import com.medidropbox.dto.response.StaffResponse;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.User;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.UserRepository;
import com.medidropbox.service.StaffManagementService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StaffManagementServiceImpl implements StaffManagementService {
    
    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final PasswordEncoder passwordEncoder;
    
    public StaffManagementServiceImpl(UserRepository userRepository,
                                     HospitalRepository hospitalRepository,
                                     PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public StaffResponse createStaff(Long hospitalId, CreateStaffRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists: " + request.getUsername());
        }
        
        User staff = new User();
        staff.setUsername(request.getUsername());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole());
        staff.setHospital(hospital);
        staff.setIsActive(true);
        staff.setIsFirstLogin(true);
        
        staff = userRepository.save(staff);
        
        return mapToResponse(staff);
    }
    
    @Override
    public List<StaffResponse> getStaffByHospital(Long hospitalId) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        List<User> staff = userRepository.findByHospitalId(hospitalId);
        return staff.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public StaffResponse getStaffById(Long staffId, Long hospitalId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Verify staff belongs to the hospital
        if (staff.getHospital() == null || !staff.getHospital().getId().equals(hospitalId)) {
            throw new RuntimeException("Staff does not belong to this hospital");
        }
        
        return mapToResponse(staff);
    }
    
    @Override
    public StaffResponse updateStaff(Long staffId, Long hospitalId, CreateStaffRequest request) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Verify staff belongs to the hospital
        if (staff.getHospital() == null || !staff.getHospital().getId().equals(hospitalId)) {
            throw new RuntimeException("Staff does not belong to this hospital");
        }
        
        // Update fields
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            staff.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            staff.setRole(request.getRole());
        }
        
        staff = userRepository.save(staff);
        return mapToResponse(staff);
    }
    
    @Override
    public void deactivateStaff(Long staffId, Long hospitalId) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Verify staff belongs to the hospital
        if (staff.getHospital() == null || !staff.getHospital().getId().equals(hospitalId)) {
            throw new RuntimeException("Staff does not belong to this hospital");
        }
        
        staff.setIsActive(false);
        userRepository.save(staff);
    }
    
    @Override
    public StaffResponse assignRoleToStaff(Long staffId, Long hospitalId, Role role) {
        User staff = userRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        
        // Verify staff belongs to the hospital
        if (staff.getHospital() == null || !staff.getHospital().getId().equals(hospitalId)) {
            throw new RuntimeException("Staff does not belong to this hospital");
        }
        
        staff.setRole(role);
        staff = userRepository.save(staff);
        return mapToResponse(staff);
    }
    
    private StaffResponse mapToResponse(User user) {
        return StaffResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .hospitalId(user.getHospital() != null ? user.getHospital().getId() : null)
                .hospitalName(user.getHospital() != null ? user.getHospital().getName() : null)
                .build();
    }
}
