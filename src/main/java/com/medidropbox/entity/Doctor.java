package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Doctor entity
 */
@Entity
@Table(name = "md_doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Doctor name is required")
    @Column(nullable = false)
    private String name;

    @Column(name = "title")
    private String title; // MBBS / Surgeon etc.

    @Column(columnDefinition = "TEXT")
    private String about;

    @Column(name = "specialty")
    private String specialty;

    @ElementCollection
    @CollectionTable(name = "md_doctor_services", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "service")
    private List<String> services = new ArrayList<>();

    @NotBlank(message = "Phone is required")
    @Column(nullable = false)
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ElementCollection
    @CollectionTable(name = "md_doctor_expertise", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "expertise")
    private List<String> expertise = new ArrayList<>();

    @Column(name = "served_patient_count")
    private Long servedPatientCount = 0L;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "fees", precision = 10, scale = 2)
    private BigDecimal fees;

    @Column(name = "average_consultation_time")
    private Integer averageConsultationTime; // in minutes

    @Column(name = "allow_remote")
    private Boolean allowRemote = false;

    @ElementCollection
    @CollectionTable(name = "md_doctor_languages", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "language")
    private List<String> language = new ArrayList<>();

    @NotNull(message = "Hospital is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "md_doctor_awards",
        joinColumns = @JoinColumn(name = "doctor_id"),
        inverseJoinColumns = @JoinColumn(name = "award_id")
    )
    private List<Award> awards = new ArrayList<>();
}
