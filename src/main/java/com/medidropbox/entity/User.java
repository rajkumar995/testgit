package com.medidropbox.entity;

import com.medidropbox.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Core User entity - Every human is a USER first, then mapped to a role
 * Single user table for all roles
 */
@Entity
@Table(name = "md_users", uniqueConstraints = {
    @UniqueConstraint(name = "UK_user_username", columnNames = {"username"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username; // email or phone, unique

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password; // BCrypt encrypted

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_first_login", nullable = false)
    private Boolean isFirstLogin = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    // Hospital association - for HOSPITAL_STAFF, DOCTOR roles
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private Hospital hospital;
    
    // Global Patient association - for PATIENT role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_patient_id")
    private GlobalPatient globalPatient;
}
