package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hospital (MediDropBox) entity
 */
@Entity
@Table(name = "md_hospitals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hospital extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hospital name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "founder")
    private String founder;

    @Column(name = "founded_on")
    private LocalDate foundedOn;

    @ElementCollection
    @CollectionTable(name = "md_hospital_images", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "md_hospital_social_media",
        joinColumns = @JoinColumn(name = "hospital_id"),
        inverseJoinColumns = @JoinColumn(name = "social_media_link_id")
    )
    private List<SocialMediaLink> socialMediaLinks = new ArrayList<>();

    @Column(name = "emergency_available")
    private Boolean emergencyAvailable = false;

    @Column(nullable = false)
    private String country;

    @ElementCollection
    @CollectionTable(name = "md_hospital_services", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "service")
    private List<String> services = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "md_hospital_facilities", joinColumns = @JoinColumn(name = "hospital_id"))
    @Column(name = "facility")
    private List<String> facilities = new ArrayList<>();

    @Column(name = "emergency_call_number")
    private String emergencyCallNumber;

    @Column(name = "booking_call_number")
    private String bookingCallNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
