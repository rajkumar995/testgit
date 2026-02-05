package com.medidropbox.service.impl;

import com.medidropbox.dto.request.HospitalFilterRequest;
import com.medidropbox.dto.request.HospitalRegistrationRequest;
import com.medidropbox.dto.response.HospitalResponse;
import com.medidropbox.dto.response.PagedResponse;
import com.medidropbox.entity.Address;
import com.medidropbox.entity.Hospital;
import com.medidropbox.entity.SocialMediaLink;
import com.medidropbox.entity.User;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.AddressRepository;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.SocialMediaLinkRepository;
import com.medidropbox.repository.UserRepository;
import com.medidropbox.service.HospitalService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HospitalServiceImpl implements HospitalService {
    
    private final HospitalRepository hospitalRepository;
    private final AddressRepository addressRepository;
    private final SocialMediaLinkRepository socialMediaLinkRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public HospitalServiceImpl(HospitalRepository hospitalRepository,
                              AddressRepository addressRepository,
                              SocialMediaLinkRepository socialMediaLinkRepository,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.hospitalRepository = hospitalRepository;
        this.addressRepository = addressRepository;
        this.socialMediaLinkRepository = socialMediaLinkRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public HospitalResponse registerHospital(HospitalRegistrationRequest request) {
        // Create address
        Address address = new Address();
        address.setAddressLine1(request.getAddress().getAddressLine1());
        address.setAddressLine2(request.getAddress().getAddressLine2());
        address.setCity(request.getAddress().getCity());
        address.setState(request.getAddress().getState());
        address.setCountry(request.getAddress().getCountry());
        address.setPincode(request.getAddress().getPincode());
        address.setLatitude(request.getAddress().getLatitude());
        address.setLongitude(request.getAddress().getLongitude());
        address.setLocationUrl(request.getAddress().getLocationUrl());
        address = addressRepository.save(address);
        
        // Create hospital
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setFounder(request.getFounder());
        hospital.setFoundedOn(request.getFoundedOn());
        hospital.setImages(request.getImages() != null ? request.getImages() : List.of());
        hospital.setAddress(address);
        hospital.setEmergencyAvailable(request.getEmergencyAvailable());
        hospital.setCountry(request.getCountry());
        hospital.setServices(request.getServices() != null ? request.getServices() : List.of());
        hospital.setFacilities(request.getFacilities() != null ? request.getFacilities() : List.of());
        hospital.setEmergencyCallNumber(request.getEmergencyCallNumber());
        hospital.setBookingCallNumber(request.getBookingCallNumber());
        hospital.setIsActive(true);
        
        // Save social media links if provided
        if (request.getSocialMediaLinks() != null && !request.getSocialMediaLinks().isEmpty()) {
            List<SocialMediaLink> links = request.getSocialMediaLinks().stream()
                    .map(req -> {
                        SocialMediaLink link = new SocialMediaLink();
                        link.setPlatformName(req.getPlatformName());
                        link.setProfileUrl(req.getProfileUrl());
                        return socialMediaLinkRepository.save(link);
                    })
                    .collect(Collectors.toList());
            hospital.setSocialMediaLinks(links);
        }
        
        hospital = hospitalRepository.save(hospital);
        
        // Create admin user account for the hospital (required during registration)
        String createdUsername = null;
        if (request.getAdminUsername() == null || request.getAdminUsername().trim().isEmpty()) {
            throw new RuntimeException("Admin username is required for hospital registration");
        }
        if (request.getAdminPassword() == null || request.getAdminPassword().trim().isEmpty()) {
            throw new RuntimeException("Admin password is required for hospital registration");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getAdminUsername())) {
            throw new RuntimeException("Username already exists: " + request.getAdminUsername());
        }
        
        User adminUser = new User();
        adminUser.setUsername(request.getAdminUsername());
        adminUser.setPassword(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setRole(Role.HOSPITAL_ADMIN); // Hospital admin gets HOSPITAL_ADMIN role with all permissions
        adminUser.setHospital(hospital); // Link user to hospital
        adminUser.setIsActive(true);
        adminUser.setIsFirstLogin(true);
        userRepository.save(adminUser);
        createdUsername = request.getAdminUsername();
        
        HospitalResponse response = mapToResponse(hospital, Role.PUBLIC);
        response.setAdminUsername(createdUsername); // Include username in response
        return response;
    }
    
    @Override
    public HospitalResponse getHospitalById(Long id, Role role) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        return HospitalResponse.filterByRole(mapToResponse(hospital, role), role);
    }

    @Override
    public HospitalResponse getHospitalByIdForSharedView(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        HospitalResponse response = mapToResponse(hospital, Role.PUBLIC);
        response.setBookingCallNumber(hospital.getBookingCallNumber());
        response.setEmergencyCallNumber(hospital.getEmergencyCallNumber());
        return response;
    }
    
    @Override
    public List<HospitalResponse> getAllHospitals(Role role) {
        List<Hospital> hospitals = hospitalRepository.findAllActiveHospitals();
        return hospitals.stream()
                .map(h -> HospitalResponse.filterByRole(mapToResponse(h, role), role))
                .collect(Collectors.toList());
    }
    
    @Override
    public PagedResponse<HospitalResponse> searchHospitals(HospitalFilterRequest filterRequest, Role role) {
        Specification<Hospital> spec = buildHospitalSpecification(filterRequest);
        
        // If 24x7 or ambulance filters are used, we need to fetch all and filter in memory
        // for accurate pagination. Otherwise, use standard pagination.
        boolean needsPostFiltering = (filterRequest.getIs24x7() != null && filterRequest.getIs24x7()) ||
                                     (filterRequest.getHasAmbulance() != null && filterRequest.getHasAmbulance());
        
        List<Hospital> allFilteredHospitals;
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 10;
        
        if (needsPostFiltering) {
            // Fetch all matching hospitals, then filter and paginate in memory
            List<Hospital> allHospitals = hospitalRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "name"));
            
            allFilteredHospitals = allHospitals.stream()
                    .filter(h -> {
                        // 24x7 filter
                        if (filterRequest.getIs24x7() != null && filterRequest.getIs24x7()) {
                            boolean has24x7 = h.getFacilities() != null && h.getFacilities().stream()
                                    .anyMatch(f -> f != null && (
                                            f.toLowerCase().contains("24/7") ||
                                            f.toLowerCase().contains("24*7") ||
                                            f.toLowerCase().contains("24-7") ||
                                            f.toLowerCase().contains("24x7")
                                    ));
                            if (!has24x7) return false;
                        }
                        
                        // Ambulance filter
                        if (filterRequest.getHasAmbulance() != null && filterRequest.getHasAmbulance()) {
                            boolean hasAmbulance = false;
                            if (h.getFacilities() != null) {
                                hasAmbulance = h.getFacilities().stream()
                                        .anyMatch(f -> f != null && f.toLowerCase().contains("ambulance"));
                            }
                            if (!hasAmbulance && h.getServices() != null) {
                                hasAmbulance = h.getServices().stream()
                                        .anyMatch(s -> s != null && s.toLowerCase().contains("ambulance"));
                            }
                            if (!hasAmbulance) return false;
                        }
                        
                        return true;
                    })
                    .collect(Collectors.toList());
        } else {
            // Standard pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
            Page<Hospital> hospitalPage = hospitalRepository.findAll(spec, pageable);
            allFilteredHospitals = hospitalPage.getContent();
            
            List<HospitalResponse> content = allFilteredHospitals.stream()
                    .map(h -> HospitalResponse.filterByRole(mapToResponse(h, role), role))
                    .collect(Collectors.toList());
            
            return PagedResponse.<HospitalResponse>builder()
                    .content(content)
                    .page(hospitalPage.getNumber())
                    .size(hospitalPage.getSize())
                    .totalElements(hospitalPage.getTotalElements())
                    .totalPages(hospitalPage.getTotalPages())
                    .first(hospitalPage.isFirst())
                    .last(hospitalPage.isLast())
                    .build();
        }
        
        // Apply pagination to filtered results
        int totalElements = allFilteredHospitals.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<Hospital> paginatedHospitals = start < totalElements 
                ? allFilteredHospitals.subList(start, end)
                : new ArrayList<>();
        
        List<HospitalResponse> content = paginatedHospitals.stream()
                .map(h -> HospitalResponse.filterByRole(mapToResponse(h, role), role))
                .collect(Collectors.toList());
        
        return PagedResponse.<HospitalResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
    
    private Specification<Hospital> buildHospitalSpecification(HospitalFilterRequest filterRequest) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Join with address for location-based filters
            Join<Hospital, Address> addressJoin = root.join("address", JoinType.INNER);
            
            // State filter
            if (filterRequest.getState() != null && !filterRequest.getState().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(addressJoin.get("state")), filterRequest.getState().toLowerCase()));
            }
            
            // City filter
            if (filterRequest.getCity() != null && !filterRequest.getCity().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(addressJoin.get("city")), filterRequest.getCity().toLowerCase()));
            }
            
            // Pincode filter
            if (filterRequest.getPincode() != null && !filterRequest.getPincode().trim().isEmpty()) {
                predicates.add(cb.equal(addressJoin.get("pincode"), filterRequest.getPincode()));
            }
            
            // Emergency available filter
            if (filterRequest.getEmergencyAvailable() != null) {
                predicates.add(cb.equal(root.get("emergencyAvailable"), filterRequest.getEmergencyAvailable()));
            }
            
            // Active filter (default to true if not specified)
            if (filterRequest.getIsActive() != null) {
                predicates.add(cb.equal(root.get("isActive"), filterRequest.getIsActive()));
            } else {
                predicates.add(cb.equal(root.get("isActive"), true));
            }
            
            // Location-based filter (latitude/longitude with radius)
            if (filterRequest.getLatitude() != null && filterRequest.getLongitude() != null) {
                // Using Haversine formula for distance calculation
                // This is a simplified version - for production, consider using PostGIS or similar
                // Note: This is a basic implementation. For accurate distance calculation, 
                // you might want to use a spatial database extension
                predicates.add(cb.isNotNull(addressJoin.get("latitude")));
                predicates.add(cb.isNotNull(addressJoin.get("longitude")));
            }
            
            // Note: 24x7 and ambulance filters are handled in post-filtering
            // due to JPA limitations with array/collection contains checks
            
            // Search on name
            if (filterRequest.getSearch() != null && !filterRequest.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filterRequest.getSearch().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), searchTerm));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    @Override
    public HospitalResponse updateHospital(Long id, HospitalRegistrationRequest request, Role role) {
        // ADMIN, PRODUCT_ADMIN, SUPER_ADMIN can update any hospital
        // HOSPITAL_ADMIN can update their own hospital (verified in controller)
        if (role != Role.ADMIN && role != Role.PRODUCT_ADMIN && role != Role.SUPER_ADMIN && role != Role.HOSPITAL_ADMIN) {
            throw new RuntimeException("Insufficient permissions");
        }
        
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        // Update only fields that are explicitly provided (non-null)
        if (request.getName() != null) {
            hospital.setName(request.getName());
        }
        if (request.getFounder() != null) {
            hospital.setFounder(request.getFounder());
        }
        if (request.getFoundedOn() != null) {
            hospital.setFoundedOn(request.getFoundedOn());
        }
        if (request.getImages() != null) {
            hospital.setImages(request.getImages());
        }
        if (request.getEmergencyAvailable() != null) {
            hospital.setEmergencyAvailable(request.getEmergencyAvailable());
        }
        if (request.getCountry() != null) {
            hospital.setCountry(request.getCountry());
        }
        if (request.getServices() != null) {
            hospital.setServices(request.getServices());
        }
        if (request.getFacilities() != null) {
            hospital.setFacilities(request.getFacilities());
        }
        if (request.getEmergencyCallNumber() != null) {
            hospital.setEmergencyCallNumber(request.getEmergencyCallNumber());
        }
        if (request.getBookingCallNumber() != null) {
            hospital.setBookingCallNumber(request.getBookingCallNumber());
        }
        
        // Update address fields only if address is provided and fields are non-null
        if (request.getAddress() != null) {
            Address address = hospital.getAddress();
            if (address == null) {
                // Create new address if hospital doesn't have one (shouldn't happen, but safety check)
                address = new Address();
            }
            
            HospitalRegistrationRequest.AddressRequest addressRequest = request.getAddress();
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
            if (addressRequest.getLatitude() != null) {
                address.setLatitude(addressRequest.getLatitude());
            }
            if (addressRequest.getLongitude() != null) {
                address.setLongitude(addressRequest.getLongitude());
            }
            if (addressRequest.getLocationUrl() != null) {
                address.setLocationUrl(addressRequest.getLocationUrl());
            }
            
            address = addressRepository.save(address);
            hospital.setAddress(address);
        }
        
        // Update social media links if provided
        if (request.getSocialMediaLinks() != null) {
            // Remove existing links
            if (hospital.getSocialMediaLinks() != null) {
                hospital.getSocialMediaLinks().clear();
            }
            // Add new links
            List<SocialMediaLink> links = request.getSocialMediaLinks().stream()
                    .map(req -> {
                        SocialMediaLink link = new SocialMediaLink();
                        link.setPlatformName(req.getPlatformName());
                        link.setProfileUrl(req.getProfileUrl());
                        return socialMediaLinkRepository.save(link);
                    })
                    .collect(Collectors.toList());
            hospital.setSocialMediaLinks(links);
        }
        
        hospital = hospitalRepository.save(hospital);
        
        return HospitalResponse.filterByRole(mapToResponse(hospital, role), role);
    }
    
    @Override
    public void deactivateHospital(Long id, Role role) {
        // Only ADMIN and PRODUCT_ADMIN can deactivate
        if (role != Role.ADMIN && role != Role.PRODUCT_ADMIN && role != Role.SUPER_ADMIN) {
            throw new RuntimeException("Insufficient permissions");
        }
        
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        
        hospital.setIsActive(false);
        hospitalRepository.save(hospital);
    }
    
    private HospitalResponse mapToResponse(Hospital hospital, Role role) {
        HospitalResponse.AddressResponse addressResponse = null;
        if (hospital.getAddress() != null) {
            Address addr = hospital.getAddress();
            addressResponse = HospitalResponse.AddressResponse.builder()
                    .id(addr.getId())
                    .addressLine1(addr.getAddressLine1())
                    .addressLine2(addr.getAddressLine2())
                    .city(addr.getCity())
                    .state(addr.getState())
                    .country(addr.getCountry())
                    .pincode(addr.getPincode())
                    .latitude(addr.getLatitude())
                    .longitude(addr.getLongitude())
                    .locationUrl(addr.getLocationUrl())
                    .build();
        }
        
        List<HospitalResponse.SocialMediaLinkResponse> socialMediaLinks = null;
        if (hospital.getSocialMediaLinks() != null) {
            socialMediaLinks = hospital.getSocialMediaLinks().stream()
                    .map(link -> HospitalResponse.SocialMediaLinkResponse.builder()
                            .id(link.getId())
                            .platformName(link.getPlatformName())
                            .profileUrl(link.getProfileUrl())
                            .build())
                    .collect(Collectors.toList());
        }
        
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .founder(hospital.getFounder())
                .foundedOn(hospital.getFoundedOn())
                .images(hospital.getImages())
                .address(addressResponse)
                .socialMediaLinks(socialMediaLinks)
                .emergencyAvailable(hospital.getEmergencyAvailable())
                .country(hospital.getCountry())
                .services(hospital.getServices())
                .facilities(hospital.getFacilities())
                .emergencyCallNumber(hospital.getEmergencyCallNumber())
                .bookingCallNumber(hospital.getBookingCallNumber())
                .isActive(hospital.getIsActive())
                .build();
    }
}
