package com.medidropbox.entity;

import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Role-Permission mapping entity
 * Used with @PreAuthorize for fine-grained access control
 */
@Entity
@Table(name = "md_role_permissions", uniqueConstraints = {
    @UniqueConstraint(name = "UK_role_permission", columnNames = {"role", "permission"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Permission permission;
}
