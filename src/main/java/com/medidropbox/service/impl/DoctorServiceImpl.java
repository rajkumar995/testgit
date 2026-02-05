package com.medidropbox.service.impl;

import com.medidropbox.dto.request.DoctorFilterRequest;
import com.medidropbox.dto.response.DoctorResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.entity.*;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.*;
import com.medidropbox.service.DoctorService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorServiceImpl implements DoctorService {
    
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final AddressRepository addressRepository;
    private final AwardRepository awardRepository;
    
    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             HospitalRepository hospitalRepository,
                             AddressRepository addressRepository,
                             AwardRepository awardRepository) {
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.addressRepository = addressRepository;
        this.awardRepository = awardRepository;
    }
    
    @Override
    public DoctorResponse getDoctorById(Long id, Role role) {
        Doctor doctor = doctorRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found or inactive"));
        
        return DoctorResponse.filterByRole(mapToResponse(doctor), role);
    }
    
    @Override
    public List<DoctorResponse> getDoctorsByHospital(Long hospitalId, Role role) {
        List<Doctor> doctors = doctorRepository.findActiveDoctorsByHospital(hospitalId);
        
        return doctors.stream()
                .map(this::mapToResponse)
                .map(d -> DoctorResponse.filterByRole(d, role))
                .collect(Collectors.toList());
    }
    
    @Override
    public PagedResponse<DoctorResponse> searchDoctors(DoctorFilterRequest filterRequest, Role role) {
        Specification<Doctor> spec = buildDoctorSpecification(filterRequest);
        Pageable pageable = PageRequest.of(
                filterRequest.getPage() != null ? filterRequest.getPage() : 0,
                filterRequest.getSize() != null ? filterRequest.getSize() : 10,
                Sort.by(Sort.Direction.ASC, "name")
        );
        
        Page<Doctor> doctorPage = doctorRepository.findAll(spec, pageable);
        
        List<DoctorResponse> content = doctorPage.getContent().stream()
                .map(this::mapToResponse)
                .map(d -> DoctorResponse.filterByRole(d, role))
                .collect(Collectors.toList());
        
        return PagedResponse.<DoctorResponse>builder()
                .content(content)
                .page(doctorPage.getNumber())
                .size(doctorPage.getSize())
                .totalElements(doctorPage.getTotalElements())
                .totalPages(doctorPage.getTotalPages())
                .first(doctorPage.isFirst())
                .last(doctorPage.isLast())
                .build();
    }
    
    private Specification<Doctor> buildDoctorSpecification(DoctorFilterRequest filterRequest) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Hospital filter - if hospitalId is provided, filter by it; otherwise, get all doctors
            if (filterRequest.getHospitalId() != null) {
                predicates.add(cb.equal(root.get("hospital").get("id"), filterRequest.getHospitalId()));
            }
            
            // Search on name
            if (filterRequest.getSearch() != null && !filterRequest.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filterRequest.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }
            
            // Specialty filter
            if (filterRequest.getSpecialty() != null && !filterRequest.getSpecialty().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("specialty")), filterRequest.getSpecialty().toLowerCase()));
            }
            
            // Rating filters
            if (filterRequest.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filterRequest.getMinRating()));
            }
            if (filterRequest.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), filterRequest.getMaxRating()));
            }
            
            // Active filter (default to true if not specified)
            if (filterRequest.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterRequest.getIsActive()));
            } else {
                predicates.add(cb.equal(root.get("isActive"), true));
            }
            
            // Fees range filter
            if (filterRequest.getMinFees() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fees"), filterRequest.getMinFees()));
            }
            if (filterRequest.getMaxFees() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fees"), filterRequest.getMaxFees()));
            }
            
            // Allow remote filter
            if (filterRequest.getAllowRemote() != null) {
                predicates.add(cb.equal(root.get("allowRemote"), filterRequest.getAllowRemote()));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @Override
    public DoctorResponse createDoctor(DoctorResponse request, Role role, Long userHospitalId) {
        // ADMIN, PRODUCT_ADMIN, SUPER_ADMIN can create for any hospital
        // HOSPITAL_ADMIN and HOSPITAL_STAFF can only create for their own hospital
        if (role == Role.HOSPITAL_ADMIN || role == Role.HOSPITAL_STAFF) {
            if (userHospitalId == null || !userHospitalId.equals(request.getHospitalId())) {
                throw new RuntimeException(role.name() + " can only create doctors for their own hospital");
            }
        } else if (role != Role.ADMIN && role != Role.PRODUCT_ADMIN && role != Role.SUPER_ADMIN) {
            throw new RuntimeException("Insufficient permissions");
        }
        
        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setTitle(request.getTitle());
        doctor.setAbout(request.getAbout());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setServices(request.getServices() != null ? request.getServices() : List.of());
        doctor.setPhone(request.getPhone());
        doctor.setEmail(request.getEmail());
        doctor.setExpertise(request.getExpertise() != null ? request.getExpertise() : List.of());
        doctor.setServedPatientCount(request.getServedPatientCount() != null ? request.getServedPatientCount() : 0L);
        doctor.setRating(request.getRating());
        doctor.setProfilePhotoUrl(request.getProfilePhotoUrl());
        doctor.setIsActive(true);
        doctor.setFees(request.getFees());
        doctor.setAverageConsultationTime(request.getAverageConsultationTime());
        doctor.setAllowRemote(request.getAllowRemote() != null ? request.getAllowRemote() : false);
        doctor.setLanguage(request.getLanguage() != null ? request.getLanguage() : List.of());
        doctor.setHospital(hospital);
        
        // Handle address if provided
        if (request.getAddress() != null) {
            Address address = new Address();
            address.setAddressLine1(request.getAddress().getAddressLine1());
            address.setAddressLine2(request.getAddress().getAddressLine2());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setCountry(request.getAddress().getCountry());
            address.setPincode(request.getAddress().getPincode());
            address = addressRepository.save(address);
            doctor.setAddress(address);
        }
        
        doctor = doctorRepository.save(doctor);
        
        return DoctorResponse.filterByRole(mapToResponse(doctor), role);
    }
    
    @Override
    public DoctorResponse updateDoctor(Long id, DoctorResponse request, Role role, Long currentUserId, Long userHospitalId) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Permission checks
        if (role == Role.DOCTOR) {
            // Doctor can only update themselves (in a real system, check if currentUserId matches doctor's user account)
            // For now, we'll allow doctors to update their own profile
        } else if (role == Role.HOSPITAL_ADMIN) {
            // HOSPITAL_ADMIN can only update doctors from their own hospital
            if (userHospitalId == null || !userHospitalId.equals(doctor.getHospital().getId())) {
                throw new RuntimeException("HOSPITAL_ADMIN can only update doctors from their own hospital");
            }
        } else if (role != Role.ADMIN && role != Role.PRODUCT_ADMIN && role != Role.SUPER_ADMIN) {
            throw new RuntimeException("Insufficient permissions");
        }
        
        // Update only fields that are explicitly provided (non-null)
        if (request.getName() != null) {
            doctor.setName(request.getName());
        }
        if (request.getTitle() != null) {
            doctor.setTitle(request.getTitle());
        }
        if (request.getAbout() != null) {
            doctor.setAbout(request.getAbout());
        }
        if (request.getSpecialty() != null) {
            doctor.setSpecialty(request.getSpecialty());
        }
        if (request.getServices() != null) {
            doctor.setServices(request.getServices());
        }
        if (request.getPhone() != null) {
            doctor.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            doctor.setEmail(request.getEmail());
        }
        if (request.getExpertise() != null) {
            doctor.setExpertise(request.getExpertise());
        }
        if (request.getServedPatientCount() != null) {
            doctor.setServedPatientCount(request.getServedPatientCount());
        }
        if (request.getRating() != null) {
            doctor.setRating(request.getRating());
        }
        if (request.getProfilePhotoUrl() != null) {
            doctor.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }
        if (request.getFees() != null) {
            doctor.setFees(request.getFees());
        }
        if (request.getAverageConsultationTime() != null) {
            doctor.setAverageConsultationTime(request.getAverageConsultationTime());
        }
        if (request.getAllowRemote() != null) {
            doctor.setAllowRemote(request.getAllowRemote());
        }
        if (request.getLanguage() != null) {
            doctor.setLanguage(request.getLanguage());
        }
        if (request.getIsActive() != null) {
            doctor.setIsActive(request.getIsActive());
        }
        
        // Update address fields only if address is provided and fields are non-null
        if (request.getAddress() != null) {
            Address address = doctor.getAddress();
            if (address == null) {
                // Create new address if doctor doesn't have one
                address = new Address();
            }
            
            DoctorResponse.AddressResponse addressRequest = request.getAddress();
            if (addressRequest.getAddressLine1() != null) {
                address.setAddressLine1(addressRequest.getAddressLine1());
            }
            if (addressRequest.getAddressLine2() != null) {
                address.setAddressLine2(addressRequest.getAddressLine2());
            }
            if (addressRequest.getCity() != null) {
                address.setCity(addressRequest.getCity());
            }
            if (addressRequest.getState() != null) {
                address.setState(addressRequest.getState());
            }
            if (addressRequest.getCountry() != null) {
                address.setCountry(addressRequest.getCountry());
            }
            if (addressRequest.getPincode() != null) {
                address.setPincode(addressRequest.getPincode());
            }
            
            address = addressRepository.save(address);
            doctor.setAddress(address);
        }
        
        // Update awards if provided (replace entire list)
        if (request.getAwards() != null) {
            // Remove existing awards
            if (doctor.getAwards() != null) {
                doctor.getAwards().clear();
            }
            // Add new awards
            List<Award> awards = request.getAwards().stream()
                    .map(awardReq -> {
                        Award award = new Award();
                        award.setAwardName(awardReq.getAwardName());
                        award.setAwardedBy(awardReq.getAwardedBy());
                        award.setAwardYear(awardReq.getAwardYear());
                        award.setAwardImageUrl(awardReq.getAwardImageUrl());
                        return awardRepository.save(award);
                    })
                    .collect(Collectors.toList());
            doctor.setAwards(awards);
        }
        
        // Note: hospitalId is not updatable - doctor belongs to a specific hospital
        
        doctor = doctorRepository.save(doctor);
        
        return DoctorResponse.filterByRole(mapToResponse(doctor), role);
    }
    
    @Override
    public void deleteDoctor(Long id, Role role, Long userHospitalId) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        
        // Permission checks
        if (role == Role.HOSPITAL_ADMIN) {
            // HOSPITAL_ADMIN can only delete doctors from their own hospital
            if (userHospitalId == null || !userHospitalId.equals(doctor.getHospital().getId())) {
                throw new RuntimeException("HOSPITAL_ADMIN can only delete doctors from their own hospital");
            }
        } else if (role != Role.ADMIN && role != Role.PRODUCT_ADMIN && role != Role.SUPER_ADMIN) {
            throw new RuntimeException("Insufficient permissions");
        }
        
        // Deactivate doctor (soft delete)
        doctor.setIsActive(false);
        doctorRepository.save(doctor);
    }
    
    private DoctorResponse mapToResponse(Doctor doctor) {
        DoctorResponse.AddressResponse addressResponse = null;
        if (doctor.getAddress() != null) {
            Address addr = doctor.getAddress();
            addressResponse = DoctorResponse.AddressResponse.builder()
                    .id(addr.getId())
                    .addressLine1(addr.getAddressLine1())
                    .addressLine2(addr.getAddressLine2())
                    .city(addr.getCity())
                    .state(addr.getState())
                    .country(addr.getCountry())
                    .pincode(addr.getPincode())
                    .build();
        }
        
        List<DoctorResponse.AwardResponse> awards = null;
        if (doctor.getAwards() != null) {
            awards = doctor.getAwards().stream()
                    .map(award -> DoctorResponse.AwardResponse.builder()
                            .id(award.getId())
                            .awardName(award.getAwardName())
                            .awardedBy(award.getAwardedBy())
                            .awardYear(award.getAwardYear())
                            .awardImageUrl(award.getAwardImageUrl())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return DoctorResponse.builder()
                .id(doctor.getId())
                .name(doctor.getName())
                .title(doctor.getTitle())
                .about(doctor.getAbout())
                .specialty(doctor.getSpecialty())
                .services(doctor.getServices())
                .phone(doctor.getPhone())
                .email(doctor.getEmail())
                .address(addressResponse)
                .expertise(doctor.getExpertise())
                .servedPatientCount(doctor.getServedPatientCount())
                .rating(doctor.getRating())
                .profilePhotoUrl(doctor.getProfilePhotoUrl())
                .isActive(doctor.getIsActive())
                .fees(doctor.getFees())
                .averageConsultationTime(doctor.getAverageConsultationTime())
                .allowRemote(doctor.getAllowRemote())
                .language(doctor.getLanguage())
                .hospitalId(doctor.getHospital().getId())
                .hospitalName(doctor.getHospital().getName())
                .awards(awards)
                .build();
    }
}
