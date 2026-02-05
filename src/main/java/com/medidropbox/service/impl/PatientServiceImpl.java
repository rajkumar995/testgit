package com.medidropbox.service.impl;

import com.medidropbox.dto.request.AddressRequest;
import com.medidropbox.dto.request.EmergencyContactRequest;
import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.response.AddressResponse;
import com.medidropbox.dto.response.EmergencyContactResponse;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.entity.Address;
import com.medidropbox.entity.EmergencyContact;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.User;
import com.medidropbox.enums.PatientStatus;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.AddressRepository;
import com.medidropbox.repository.EmergencyContactRepository;
import com.medidropbox.repository.GlobalPatientRepository;
import com.medidropbox.repository.UserRepository;
import com.medidropbox.service.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientServiceImpl implements PatientService {
    
    private final GlobalPatientRepository globalPatientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Still needed for User entity (dummy password)
    private final AddressRepository addressRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    
    public PatientServiceImpl(GlobalPatientRepository globalPatientRepository,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder,
                             AddressRepository addressRepository,
                             EmergencyContactRepository emergencyContactRepository) {
        this.globalPatientRepository = globalPatientRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
        this.emergencyContactRepository = emergencyContactRepository;
    }
    
    @Override
    public GlobalPatient createOrGetGlobalPatient(String phone, String email, String fullName) {
        // Check if global patient already exists
        Optional<GlobalPatient> existing = globalPatientRepository.findByPhone(phone);
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Create new UNCLAIMED global patient (created by hospital)
        GlobalPatient globalPatient = new GlobalPatient();
        globalPatient.setPhone(phone);
        globalPatient.setEmail(email);
        globalPatient.setFullName(fullName != null ? fullName : "Patient");
        globalPatient.setIsActive(true);
        globalPatient.setStatus(PatientStatus.UNCLAIMED); // Not yet registered by patient
        
        return globalPatientRepository.save(globalPatient);
    }
    
    @Override
    public Optional<GlobalPatient> getGlobalPatientById(Long id) {
        return globalPatientRepository.findById(id);
    }
    
    @Override
    public Optional<GlobalPatient> getGlobalPatientByPhone(String phone) {
        return globalPatientRepository.findByPhone(phone);
    }
    
    /**
     * Find duplicate global patient by phone, aadharId, or abhaId
     */
    private Optional<GlobalPatient> findDuplicateGlobalPatient(PatientRequest request) {
        // Check by phone
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            Optional<GlobalPatient> byPhone = globalPatientRepository.findByPhone(request.getPhone());
            if (byPhone.isPresent()) {
                return byPhone;
            }
        }
        
        // Check by Aadhar ID
        if (request.getAadharId() != null && !request.getAadharId().trim().isEmpty()) {
            Optional<GlobalPatient> byAadhar = globalPatientRepository.findByAadharId(request.getAadharId());
            if (byAadhar.isPresent()) {
                return byAadhar;
            }
        }
        
        // Check by ABHA ID
        if (request.getAbhaId() != null && !request.getAbhaId().trim().isEmpty()) {
            Optional<GlobalPatient> byAbha = globalPatientRepository.findByAbhaId(request.getAbhaId());
            if (byAbha.isPresent()) {
                return byAbha;
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public PatientResponse registerPatient(PatientRequest request) {
        // Check for duplicate global patient by phone, aadharId, or abhaId
        Optional<GlobalPatient> duplicatePatient = findDuplicateGlobalPatient(request);
        if (duplicatePatient.isPresent()) {
            GlobalPatient existing = duplicatePatient.get();
            if (existing.getStatus() == PatientStatus.CLAIMED) {
                throw new RuntimeException("Patient already registered with this phone, Aadhar ID, or ABHA ID");
            }
            // If UNCLAIMED, we'll claim it below
        }
        
        // Check if UNCLAIMED patient exists with same phone
        Optional<GlobalPatient> unclaimedPatient = globalPatientRepository.findByPhoneAndStatus(
            request.getPhone(), PatientStatus.UNCLAIMED);
        
        GlobalPatient globalPatient;
        
        if (unclaimedPatient.isPresent()) {
            // CLAIM the existing patient (hospital created it first)
            globalPatient = unclaimedPatient.get();
            globalPatient.setStatus(PatientStatus.CLAIMED);
            
            // Update profile if provided
            if (request.getFullName() != null) {
                globalPatient.setFullName(request.getFullName());
            }
            if (request.getEmail() != null) {
                globalPatient.setEmail(request.getEmail());
            }
            if (request.getProfileImageUrl() != null) {
                globalPatient.setProfileImageUrl(request.getProfileImageUrl());
            }
            if (request.getAadharId() != null) {
                // Check if Aadhar ID already exists
                Optional<GlobalPatient> existingByAadhar = globalPatientRepository.findByAadharId(request.getAadharId());
                if (existingByAadhar.isPresent() && !existingByAadhar.get().getId().equals(globalPatient.getId())) {
                    throw new RuntimeException("Aadhar ID already exists: " + request.getAadharId());
                }
                globalPatient.setAadharId(request.getAadharId());
            }
            if (request.getAbhaId() != null) {
                // Check if ABHA ID already exists
                Optional<GlobalPatient> existingByAbha = globalPatientRepository.findByAbhaId(request.getAbhaId());
                if (existingByAbha.isPresent() && !existingByAbha.get().getId().equals(globalPatient.getId())) {
                    throw new RuntimeException("ABHA ID already exists: " + request.getAbhaId());
                }
                globalPatient.setAbhaId(request.getAbhaId());
            }
            // Update address if provided
            if (request.getAddress() != null) {
                Address address;
                if (globalPatient.getAddress() != null) {
                    // Update existing address
                    address = globalPatient.getAddress();
                    updateAddressFromRequest(address, request.getAddress());
                } else {
                    // Create new address
                    address = mapToAddress(request.getAddress());
                }
                address = addressRepository.save(address);
                globalPatient.setAddress(address);
            }
            // Update emergency contacts if provided
            if (request.getEmergencyContacts() != null && !request.getEmergencyContacts().isEmpty()) {
                // Clear existing emergency contacts
                globalPatient.getEmergencyContacts().clear();
                // Add new emergency contacts
                for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
                    EmergencyContact emergencyContact = mapToEmergencyContact(ecRequest, globalPatient);
                    globalPatient.getEmergencyContacts().add(emergencyContact);
                }
            }
        } else {
            // Check for duplicate by phone, aadharId, or abhaId (already checked above, but double-check)
            Optional<GlobalPatient> duplicate = findDuplicateGlobalPatient(request);
            if (duplicate.isPresent() && duplicate.get().getStatus() == PatientStatus.CLAIMED) {
                throw new RuntimeException("Patient already registered with this phone, Aadhar ID, or ABHA ID");
            }
            
            // Validate ABHA ID uniqueness if provided
            if (request.getAbhaId() != null) {
                Optional<GlobalPatient> existingByAbha = globalPatientRepository.findByAbhaId(request.getAbhaId());
                if (existingByAbha.isPresent()) {
                    throw new RuntimeException("ABHA ID already exists: " + request.getAbhaId());
                }
            }
            
            // Create new CLAIMED patient
            globalPatient = new GlobalPatient();
            globalPatient.setFullName(request.getFullName());
            globalPatient.setPhone(request.getPhone());
            globalPatient.setEmail(request.getEmail());
            globalPatient.setProfileImageUrl(request.getProfileImageUrl());
            globalPatient.setAadharId(request.getAadharId());
            globalPatient.setAbhaId(request.getAbhaId());
            globalPatient.setIsActive(true);
            globalPatient.setStatus(PatientStatus.CLAIMED);
            
            // Set address if provided
            if (request.getAddress() != null) {
                Address address = mapToAddress(request.getAddress());
                address = addressRepository.save(address);
                globalPatient.setAddress(address);
            }
            
            // Add emergency contacts if provided
            if (request.getEmergencyContacts() != null && !request.getEmergencyContacts().isEmpty()) {
                for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
                    EmergencyContact emergencyContact = mapToEmergencyContact(ecRequest, globalPatient);
                    globalPatient.getEmergencyContacts().add(emergencyContact);
                }
            }
        }
        
        globalPatient = globalPatientRepository.save(globalPatient);
        
        // Create User account for patient login (with dummy password - OTP will be used for auth)
        if (!userRepository.existsByUsername(request.getPhone())) {
            User user = new User();
            user.setUsername(request.getPhone()); // Use phone as username
            // Dummy password - actual authentication uses OTP
            user.setPassword(passwordEncoder.encode("OTP_AUTH_" + request.getPhone()));
            user.setRole(Role.PATIENT);
            user.setGlobalPatient(globalPatient); // Link to global patient
            user.setIsActive(true);
            user.setIsFirstLogin(true);
            userRepository.save(user);
        }
        
        return mapToResponse(globalPatient);
    }
    
    @Override
    public GlobalPatient claimPatient(String phone) {
        Optional<GlobalPatient> unclaimedPatient = globalPatientRepository.findByPhoneAndStatus(
            phone, PatientStatus.UNCLAIMED);
        
        if (unclaimedPatient.isEmpty()) {
            throw new RuntimeException("No unclaimed patient found with phone: " + phone);
        }
        
        GlobalPatient globalPatient = unclaimedPatient.get();
        globalPatient.setStatus(PatientStatus.CLAIMED);
        
        return globalPatientRepository.save(globalPatient);
    }
    
    @Override
    public PatientResponse getPatientById(Long id) {
        GlobalPatient globalPatient = globalPatientRepository.findByIdWithAddressAndContacts(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        return mapToResponse(globalPatient);
    }
    
    @Override
    public PatientResponse getPatientByPhone(String phone) {
        GlobalPatient globalPatient = globalPatientRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Patient not found with phone: " + phone));
        return mapToResponse(globalPatient);
    }
    
    @Override
    public List<PatientResponse> getAllPatients() {
        List<GlobalPatient> patients = globalPatientRepository.findAllByIsActiveTrue();
        return patients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PatientResponse updatePatient(Long id, PatientRequest request) {
        GlobalPatient globalPatient = globalPatientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        
        // Only CLAIMED patients can update their profile
        if (globalPatient.getStatus() != PatientStatus.CLAIMED) {
            throw new RuntimeException("Only registered patients can update their profile");
        }
        
        // Update only fields that are explicitly provided (non-null)
        if (request.getFullName() != null) {
            globalPatient.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            // Check if new phone is already taken by another patient
            Optional<GlobalPatient> existingByPhone = globalPatientRepository.findByPhone(request.getPhone());
            if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(id)) {
                throw new RuntimeException("Phone number already exists: " + request.getPhone());
            }
            globalPatient.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            // Check if new email is already taken by another patient
            Optional<GlobalPatient> existingByEmail = globalPatientRepository.findByEmail(request.getEmail());
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
            globalPatient.setEmail(request.getEmail());
        }
        if (request.getProfileImageUrl() != null) {
            globalPatient.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getAadharId() != null) {
            // Check if Aadhar ID already exists
            Optional<GlobalPatient> existingByAadhar = globalPatientRepository.findByAadharId(request.getAadharId());
            if (existingByAadhar.isPresent() && !existingByAadhar.get().getId().equals(id)) {
                throw new RuntimeException("Aadhar ID already exists: " + request.getAadharId());
            }
            globalPatient.setAadharId(request.getAadharId());
        }
        if (request.getAbhaId() != null) {
            // Check if ABHA ID already exists
            Optional<GlobalPatient> existingByAbha = globalPatientRepository.findByAbhaId(request.getAbhaId());
            if (existingByAbha.isPresent() && !existingByAbha.get().getId().equals(id)) {
                throw new RuntimeException("ABHA ID already exists: " + request.getAbhaId());
            }
            globalPatient.setAbhaId(request.getAbhaId());
        }
        // Update address if provided
        if (request.getAddress() != null) {
            Address address;
            if (globalPatient.getAddress() != null) {
                // Update existing address
                address = globalPatient.getAddress();
                updateAddressFromRequest(address, request.getAddress());
            } else {
                // Create new address
                address = mapToAddress(request.getAddress());
            }
            address = addressRepository.save(address);
            globalPatient.setAddress(address);
        }
        // Update emergency contacts if provided
        if (request.getEmergencyContacts() != null) {
            // Clear existing emergency contacts
            globalPatient.getEmergencyContacts().clear();
            // Add new emergency contacts
            for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
                EmergencyContact emergencyContact = mapToEmergencyContact(ecRequest, globalPatient);
                globalPatient.getEmergencyContacts().add(emergencyContact);
            }
        }
        
        globalPatient = globalPatientRepository.save(globalPatient);
        return mapToResponse(globalPatient);
    }
    
    @Override
    public void deactivatePatient(Long id) {
        GlobalPatient globalPatient = globalPatientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        globalPatient.setIsActive(false);
        globalPatientRepository.save(globalPatient);
    }
    
    private PatientResponse mapToResponse(GlobalPatient globalPatient) {
        // Map address
        AddressResponse addressResponse = null;
        if (globalPatient.getAddress() != null) {
            addressResponse = mapToAddressResponse(globalPatient.getAddress());
        }
        
        // Map emergency contacts
        List<EmergencyContactResponse> emergencyContactsResponse = null;
        if (globalPatient.getEmergencyContacts() != null && !globalPatient.getEmergencyContacts().isEmpty()) {
            emergencyContactsResponse = globalPatient.getEmergencyContacts().stream()
                    .filter(ec -> ec.getIsActive())
                    .map(this::mapToEmergencyContactResponse)
                    .collect(Collectors.toList());
        }
        
        return PatientResponse.builder()
                .id(globalPatient.getId())
                .fullName(globalPatient.getFullName())
                .phone(globalPatient.getPhone())
                .email(globalPatient.getEmail())
                .profileImageUrl(globalPatient.getProfileImageUrl())
                .aadharId(globalPatient.getAadharId())
                .abhaId(globalPatient.getAbhaId())
                .address(addressResponse)
                .emergencyContacts(emergencyContactsResponse)
                .isActive(globalPatient.getIsActive())
                .createdAt(globalPatient.getCreatedAt())
                .updatedAt(globalPatient.getUpdatedAt())
                .build();
    }
    
    private Address mapToAddress(AddressRequest request) {
        Address address = new Address();
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPincode(request.getPincode());
        address.setLatitude(request.getLatitude());
        address.setLongitude(request.getLongitude());
        address.setLocationUrl(request.getLocationUrl());
        return address;
    }
    
    private void updateAddressFromRequest(Address address, AddressRequest request) {
        if (request.getAddressLine1() != null) {
            address.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            address.setAddressLine2(request.getAddressLine2());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getCountry() != null) {
            address.setCountry(request.getCountry());
        }
        if (request.getPincode() != null) {
            address.setPincode(request.getPincode());
        }
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }
        if (request.getLocationUrl() != null) {
            address.setLocationUrl(request.getLocationUrl());
        }
    }
    
    private AddressResponse mapToAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .locationUrl(address.getLocationUrl())
                .build();
    }
    
    private EmergencyContact mapToEmergencyContact(EmergencyContactRequest request, GlobalPatient globalPatient) {
        EmergencyContact emergencyContact = new EmergencyContact();
        emergencyContact.setGlobalPatient(globalPatient);
        emergencyContact.setPersonName(request.getPersonName());
        emergencyContact.setPhone(request.getPhone());
        emergencyContact.setRelationship(request.getRelationship());
        emergencyContact.setEmail(request.getEmail());
        emergencyContact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        emergencyContact.setIsActive(true);
        return emergencyContact;
    }
    
    private EmergencyContactResponse mapToEmergencyContactResponse(EmergencyContact emergencyContact) {
        return EmergencyContactResponse.builder()
                .id(emergencyContact.getId())
                .personName(emergencyContact.getPersonName())
                .phone(emergencyContact.getPhone())
                .relationship(emergencyContact.getRelationship())
                .email(emergencyContact.getEmail())
                .isPrimary(emergencyContact.getIsPrimary())
                .isActive(emergencyContact.getIsActive())
                .createdAt(emergencyContact.getCreatedAt())
                .updatedAt(emergencyContact.getUpdatedAt())
                .build();
    }
}
