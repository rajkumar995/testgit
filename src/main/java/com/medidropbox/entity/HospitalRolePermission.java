package com.medidropbox.entity;

import com.medidropbox.enums.Permission;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Hospital Role-Permission mapping entity
 * Maps permissions to custom hospital roles
 */
@Entity
@Table(name = "md_hospital_role_permissions", uniqueConstraints = {
    @UniqueConstraint(name = "UK_hospital_role_permission", columnNames = {"hospital_role_id", "permission"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRolePermission extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_role_id", nullable = false)
    private HospitalRole hospitalRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;
}

