package com.medidropbox.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hospital-specific custom roles entity
 * Allows hospitals to create their own roles for staff management
 */
@Entity
@Table(name = "md_hospital_roles", uniqueConstraints = {
    @UniqueConstraint(name = "UK_hospital_role_name", columnNames = {"hospital_id", "role_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRole extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @NotBlank(message = "Role name is required")
    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

