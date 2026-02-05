package com.medidropbox.service.impl;

import com.medidropbox.dto.request.AddressRequest;
import com.medidropbox.dto.request.EmergencyContactRequest;
import com.medidropbox.dto.request.PatientRequest;
import com.medidropbox.dto.response.AddressResponse;
import com.medidropbox.dto.response.EmergencyContactResponse;
import com.medidropbox.dto.response.PatientResponse;
import com.medidropbox.entity.*;
import com.medidropbox.repository.*;
import com.medidropbox.service.HospitalPatientService;
import com.medidropbox.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class HospitalPatientServiceImpl implements HospitalPatientService {
    
    private final HospitalPatientRepository hospitalPatientRepository;
    private final HospitalRepository hospitalRepository;
    private final GlobalPatientRepository globalPatientRepository;
    private final PatientService patientService; // For creating global patient
    private final AddressRepository addressRepository;
    private final HospitalEmergencyContactRepository hospitalEmergencyContactRepository;
    
    public HospitalPatientServiceImpl(HospitalPatientRepository hospitalPatientRepository,
                                     HospitalRepository hospitalRepository,
                                     GlobalPatientRepository globalPatientRepository,
                                     PatientService patientService,
                                     AddressRepository addressRepository,
                                     HospitalEmergencyContactRepository hospitalEmergencyContactRepository) {
        this.hospitalPatientRepository = hospitalPatientRepository;
        this.hospitalRepository = hospitalRepository;
        this.globalPatientRepository = globalPatientRepository;
        this.patientService = patientService;
        this.addressRepository = addressRepository;
        this.hospitalEmergencyContactRepository = hospitalEmergencyContactRepository;
    }
    
    /**
     * Find duplicate hospital patient by phone, aadharId, or abhaId within the same hospital
     */
    private Optional<HospitalPatient> findDuplicateHospitalPatient(Long hospitalId, PatientRequest request) {
        // Check by phone
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            Optional<HospitalPatient> byPhone = hospitalPatientRepository.findByHospitalIdAndPhone(hospitalId, request.getPhone());
            if (byPhone.isPresent()) {
                return byPhone;
            }
        }
        
        // Check by Aadhar ID
        if (request.getAadharId() != null && !request.getAadharId().trim().isEmpty()) {
            Optional<HospitalPatient> byAadhar = hospitalPatientRepository.findByHospitalIdAndAadharId(hospitalId, request.getAadharId());
            if (byAadhar.isPresent()) {
                return byAadhar;
            }
        }
        
        // Check by ABHA ID
        if (request.getAbhaId() != null && !request.getAbhaId().trim().isEmpty()) {
            Optional<HospitalPatient> byAbha = hospitalPatientRepository.findByHospitalIdAndAbhaId(hospitalId, request.getAbhaId());
            if (byAbha.isPresent()) {
                return byAbha;
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public HospitalPatient createOrGetHospitalPatient(Long hospitalId, String phone, String email, String fullName, Boolean dataConsent) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // Check if patient already exists in this hospital by phone
        Optional<HospitalPatient> existing = hospitalPatientRepository.findByHospitalAndPhone(hospital, phone);
        if (existing.isPresent()) {
            HospitalPatient existingPatient = existing.get();
            // If consent is given and patient doesn't have full data yet, update it
            if (Boolean.TRUE.equals(dataConsent) && !Boolean.TRUE.equals(existingPatient.getDataConsent())) {
                // Fetch global patient with address and emergency contacts
                GlobalPatient globalPatient = globalPatientRepository.findByIdWithAddressAndContacts(
                    existingPatient.getGlobalPatient().getId())
                    .orElse(existingPatient.getGlobalPatient());
                copyFullDataFromGlobalPatient(existingPatient, globalPatient);
                existingPatient.setDataConsent(true);
                return hospitalPatientRepository.save(existingPatient);
            }
            return existingPatient;
        }
        
        // Get or create global patient (UNCLAIMED if new)
        GlobalPatient globalPatient = patientService.createOrGetGlobalPatient(phone, email, fullName);
        
        // If consent is given, fetch global patient with address and emergency contacts
        if (Boolean.TRUE.equals(dataConsent)) {
            Optional<GlobalPatient> globalPatientWithData = globalPatientRepository.findByIdWithAddressAndContacts(globalPatient.getId());
            if (globalPatientWithData.isPresent()) {
                globalPatient = globalPatientWithData.get();
            }
        }
        
        // Create hospital-specific patient record with basic data
        HospitalPatient hospitalPatient = new HospitalPatient();
        hospitalPatient.setHospital(hospital);
        hospitalPatient.setGlobalPatient(globalPatient);
        hospitalPatient.setPhone(phone);
        hospitalPatient.setFullName(fullName != null ? fullName : globalPatient.getFullName());
        hospitalPatient.setEmail(email != null ? email : globalPatient.getEmail());
        hospitalPatient.setIsActive(true);
        hospitalPatient.setDataConsent(Boolean.TRUE.equals(dataConsent));
        
        // If consent is given, copy full data from GlobalPatient
        if (Boolean.TRUE.equals(dataConsent)) {
            copyFullDataFromGlobalPatient(hospitalPatient, globalPatient);
        }
        
        return hospitalPatientRepository.save(hospitalPatient);
    }

    @Override
    public HospitalPatient createOrGetHospitalPatientForBookForFriend(Long hospitalId, String name, String phone,
                                                                       java.time.LocalDate dateOfBirth, String email, String description) {
        HospitalPatient hp = createOrGetHospitalPatient(hospitalId, phone, email, name, false);
        hp.setDateOfBirth(dateOfBirth);
        hp.setNotes(description);
        hp.setFullName(name != null ? name : hp.getFullName());
        hp.setEmail(email != null ? email : hp.getEmail());
        return hospitalPatientRepository.save(hp);
    }
    
    @Override
    public PatientResponse createHospitalPatientWithFullData(Long hospitalId, PatientRequest request) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // Check for duplicate patient in this hospital by phone, aadharId, or abhaId
        // This prevents duplicate patients in the same hospital
        Optional<HospitalPatient> existing = findDuplicateHospitalPatient(hospitalId, request);
        if (existing.isPresent()) {
            // Patient already exists in this hospital - update with new data instead of creating duplicate
            return updateHospitalPatient(hospitalId, existing.get().getId(), request);
        }
        
        // Get or create global patient (UNCLAIMED if new)
        GlobalPatient globalPatient = patientService.createOrGetGlobalPatient(
            request.getPhone(), request.getEmail(), request.getFullName());
        
        // Create hospital-specific patient record
        HospitalPatient hospitalPatient = new HospitalPatient();
        hospitalPatient.setHospital(hospital);
        hospitalPatient.setGlobalPatient(globalPatient);
        hospitalPatient.setPhone(request.getPhone());
        hospitalPatient.setFullName(request.getFullName());
        hospitalPatient.setEmail(request.getEmail());
        hospitalPatient.setIsActive(true);
        hospitalPatient.setDataConsent(false); // Manual creation, no consent
        
        // Save Aadhar ID if provided
        if (request.getAadharId() != null && !request.getAadharId().trim().isEmpty()) {
            hospitalPatient.setAadharId(request.getAadharId());
        }
        
        // Save ABHA ID if provided
        if (request.getAbhaId() != null && !request.getAbhaId().trim().isEmpty()) {
            hospitalPatient.setAbhaId(request.getAbhaId());
        }
        
        // Save address if provided
        if (request.getAddress() != null) {
            Address address = mapToAddress(request.getAddress());
            address = addressRepository.save(address);
            hospitalPatient.setAddress(address);
        }
        
        // Save emergency contacts if provided
        if (request.getEmergencyContacts() != null && !request.getEmergencyContacts().isEmpty()) {
            for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
                HospitalEmergencyContact emergencyContact = mapToHospitalEmergencyContact(ecRequest, hospitalPatient);
                hospitalPatient.getEmergencyContacts().add(emergencyContact);
            }
        }
        
        hospitalPatient = hospitalPatientRepository.save(hospitalPatient);
        return mapToResponse(hospitalPatient);
    }
    
    /**
     * Copy full data from GlobalPatient to HospitalPatient (only if consent is given)
     */
    private void copyFullDataFromGlobalPatient(HospitalPatient hospitalPatient, GlobalPatient globalPatient) {
        // Copy Aadhar ID
        if (globalPatient.getAadharId() != null) {
            hospitalPatient.setAadharId(globalPatient.getAadharId());
        }
        
        // Copy ABHA ID
        if (globalPatient.getAbhaId() != null) {
            hospitalPatient.setAbhaId(globalPatient.getAbhaId());
        }
        
        // Copy address (create new address entity for hospital patient)
        if (globalPatient.getAddress() != null) {
            Address globalAddress = globalPatient.getAddress();
            Address hospitalAddress = new Address();
            hospitalAddress.setAddressLine1(globalAddress.getAddressLine1());
            hospitalAddress.setAddressLine2(globalAddress.getAddressLine2());
            hospitalAddress.setCity(globalAddress.getCity());
            hospitalAddress.setState(globalAddress.getState());
            hospitalAddress.setCountry(globalAddress.getCountry());
            hospitalAddress.setPincode(globalAddress.getPincode());
            hospitalAddress.setLatitude(globalAddress.getLatitude());
            hospitalAddress.setLongitude(globalAddress.getLongitude());
            hospitalAddress.setLocationUrl(globalAddress.getLocationUrl());
            hospitalAddress = addressRepository.save(hospitalAddress);
            hospitalPatient.setAddress(hospitalAddress);
        }
        
        // Copy emergency contacts (create new hospital emergency contacts)
        if (globalPatient.getEmergencyContacts() != null && !globalPatient.getEmergencyContacts().isEmpty()) {
            for (EmergencyContact globalContact : globalPatient.getEmergencyContacts()) {
                if (Boolean.TRUE.equals(globalContact.getIsActive())) {
                    HospitalEmergencyContact hospitalContact = new HospitalEmergencyContact();
                    hospitalContact.setHospitalPatient(hospitalPatient);
                    hospitalContact.setPersonName(globalContact.getPersonName());
                    hospitalContact.setPhone(globalContact.getPhone());
                    hospitalContact.setRelationship(globalContact.getRelationship());
                    hospitalContact.setEmail(globalContact.getEmail());
                    hospitalContact.setIsPrimary(globalContact.getIsPrimary());
                    hospitalContact.setIsActive(true);
                    hospitalPatient.getEmergencyContacts().add(hospitalContact);
                }
            }
        }
    }
    
    @Override
    public HospitalPatient getHospitalPatientById(Long hospitalId, Long hospitalPatientId) {
        HospitalPatient hospitalPatient = hospitalPatientRepository.findById(hospitalPatientId)
                .orElseThrow(() -> new RuntimeException("Hospital patient not found"));
        
        // Verify it belongs to the specified hospital
        if (!hospitalPatient.getHospital().getId().equals(hospitalId)) {
            throw new RuntimeException("Patient does not belong to this hospital");
        }
        
        return hospitalPatient;
    }
    
    @Override
    public HospitalPatient getHospitalPatientByPhone(Long hospitalId, String phone) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        return hospitalPatientRepository.findByHospitalAndPhone(hospital, phone)
                .orElseThrow(() -> new RuntimeException("Patient not found in this hospital"));
    }
    
    @Override
    public PatientResponse getHospitalPatientByPhoneAsResponse(Long hospitalId, String phone) {
        HospitalPatient hospitalPatient = getHospitalPatientByPhone(hospitalId, phone);
        return mapToResponse(hospitalPatient);
    }
    
    @Override
    public PatientResponse getHospitalPatientByIdAsResponse(Long hospitalId, Long hospitalPatientId) {
        HospitalPatient hospitalPatient = getHospitalPatientById(hospitalId, hospitalPatientId);
        return mapToResponse(hospitalPatient);
    }
    
    @Override
    public List<PatientResponse> getHospitalPatients(Long hospitalId) {
        List<HospitalPatient> hospitalPatients = hospitalPatientRepository.findActivePatientsByHospital(hospitalId);
        return hospitalPatients.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PatientResponse updateHospitalPatient(Long hospitalId, Long hospitalPatientId, PatientRequest request) {
        HospitalPatient hospitalPatient = getHospitalPatientById(hospitalId, hospitalPatientId);
        
        // Update only fields that are explicitly provided (non-null)
        if (request.getFullName() != null) {
            hospitalPatient.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            // Check if phone already exists in this hospital
            Optional<HospitalPatient> existing = hospitalPatientRepository.findByHospitalIdAndPhone(
                hospitalId, request.getPhone());
            if (existing.isPresent() && !existing.get().getId().equals(hospitalPatientId)) {
                throw new RuntimeException("Phone number already exists in this hospital: " + request.getPhone());
            }
            hospitalPatient.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            hospitalPatient.setEmail(request.getEmail());
        }
        if (request.getAadharId() != null) {
            hospitalPatient.setAadharId(request.getAadharId());
        }
        if (request.getAbhaId() != null) {
            hospitalPatient.setAbhaId(request.getAbhaId());
        }
        
        // Update address if provided
        if (request.getAddress() != null) {
            Address address;
            if (hospitalPatient.getAddress() != null) {
                // Update existing address
                address = hospitalPatient.getAddress();
                updateAddressFromRequest(address, request.getAddress());
            } else {
                // Create new address
                address = mapToAddress(request.getAddress());
            }
            address = addressRepository.save(address);
            hospitalPatient.setAddress(address);
        }
        
        // Update emergency contacts if provided
        if (request.getEmergencyContacts() != null) {
            // Delete existing emergency contacts from database
            List<HospitalEmergencyContact> existingContacts = new ArrayList<>(hospitalPatient.getEmergencyContacts());
            for (HospitalEmergencyContact contact : existingContacts) {
                hospitalEmergencyContactRepository.delete(contact);
            }
            hospitalPatient.getEmergencyContacts().clear();
            
            // Add new emergency contacts
            for (EmergencyContactRequest ecRequest : request.getEmergencyContacts()) {
                HospitalEmergencyContact emergencyContact = mapToHospitalEmergencyContact(ecRequest, hospitalPatient);
                hospitalPatient.getEmergencyContacts().add(emergencyContact);
            }
        }
        
        hospitalPatient = hospitalPatientRepository.save(hospitalPatient);
        return mapToResponse(hospitalPatient);
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
    
    private HospitalEmergencyContact mapToHospitalEmergencyContact(EmergencyContactRequest request, HospitalPatient hospitalPatient) {
        HospitalEmergencyContact emergencyContact = new HospitalEmergencyContact();
        emergencyContact.setHospitalPatient(hospitalPatient);
        emergencyContact.setPersonName(request.getPersonName());
        emergencyContact.setPhone(request.getPhone());
        emergencyContact.setRelationship(request.getRelationship());
        emergencyContact.setEmail(request.getEmail());
        emergencyContact.setIsPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false);
        emergencyContact.setIsActive(true);
        return emergencyContact;
    }
    
    @Override
    public void deactivateHospitalPatient(Long hospitalId, Long hospitalPatientId) {
        HospitalPatient hospitalPatient = getHospitalPatientById(hospitalId, hospitalPatientId);
        hospitalPatient.setIsActive(false);
        hospitalPatientRepository.save(hospitalPatient);
    }
    
    @Override
    public Long getGlobalPatientId(Long hospitalId, Long hospitalPatientId) {
        HospitalPatient hospitalPatient = getHospitalPatientById(hospitalId, hospitalPatientId);
        return hospitalPatient.getGlobalPatient().getId();
    }
    
    private PatientResponse mapToResponse(HospitalPatient hospitalPatient) {
        // Map address
        AddressResponse addressResponse = null;
        if (hospitalPatient.getAddress() != null) {
            Address address = hospitalPatient.getAddress();
            addressResponse = AddressResponse.builder()
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
        
        // Map emergency contacts
        List<EmergencyContactResponse> emergencyContactsResponse = null;
        if (hospitalPatient.getEmergencyContacts() != null && !hospitalPatient.getEmergencyContacts().isEmpty()) {
            emergencyContactsResponse = hospitalPatient.getEmergencyContacts().stream()
                    .filter(ec -> Boolean.TRUE.equals(ec.getIsActive()))
                    .map(ec -> EmergencyContactResponse.builder()
                            .id(ec.getId())
                            .personName(ec.getPersonName())
                            .phone(ec.getPhone())
                            .relationship(ec.getRelationship())
                            .email(ec.getEmail())
                            .isPrimary(ec.getIsPrimary())
                            .isActive(ec.getIsActive())
                            .createdAt(ec.getCreatedAt())
                            .updatedAt(ec.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return PatientResponse.builder()
                .id(hospitalPatient.getId()) // Hospital patient ID
                .globalPatientId(hospitalPatient.getGlobalPatient() != null ? hospitalPatient.getGlobalPatient().getId() : null)
                .fullName(hospitalPatient.getFullName())
                .phone(hospitalPatient.getPhone())
                .email(hospitalPatient.getEmail())
                .aadharId(hospitalPatient.getAadharId())
                .abhaId(hospitalPatient.getAbhaId())
                .address(addressResponse)
                .emergencyContacts(emergencyContactsResponse)
                .isActive(hospitalPatient.getIsActive())
                .createdAt(hospitalPatient.getCreatedAt())
                .updatedAt(hospitalPatient.getUpdatedAt())
                .build();
    }
}
