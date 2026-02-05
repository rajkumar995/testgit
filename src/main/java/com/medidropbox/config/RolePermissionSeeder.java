package com.medidropbox.config;

import com.medidropbox.entity.RolePermission;
import com.medidropbox.enums.Permission;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.RolePermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Seeds initial role-permission mappings
 * Run on application startup after schema creation
 */
@Component
@Order(1) // Run after schema creation
public class RolePermissionSeeder implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(RolePermissionSeeder.class);
    
    private final RolePermissionRepository rolePermissionRepository;
    private final TransactionTemplate transactionTemplate;
    
    public RolePermissionSeeder(RolePermissionRepository rolePermissionRepository,
                                PlatformTransactionManager transactionManager) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }
    
    @Override
    public void run(String... args) {
        try {
            // Wait a bit to ensure schema is fully created
            Thread.sleep(1000);
            
            // Check if table exists before querying
            if (!isTableExists()) {
                logger.warn("Table md_role_permissions does not exist yet. Skipping seed operation.");
                return;
            }
            
            // Only seed if no permissions exist
            seedIfNeeded();
        } catch (Exception e) {
            logger.error("Error during role-permission seeding: {}", e.getMessage(), e);
            // Don't fail the application startup if seeding fails
        }
    }
    
    private void seedIfNeeded() {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                long count = rolePermissionRepository.count();
                logger.info("Current role-permission count: {}", count);
                
                if (count == 0) {
                    logger.info("Starting role-permission seeding...");
                    seedRolePermissions();
                    logger.info("Role-permission seeding completed successfully.");
                } else {
                    logger.info("Role-permissions already exist. Skipping seed operation.");
                }
            }
        });
    }
    
    private boolean isTableExists() {
        try {
            // Try to query the table - if it doesn't exist, this will throw an exception
            // Use a separate transaction for this check
            rolePermissionRepository.count();
            return true;
        } catch (Exception e) {
            logger.debug("Table check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void seedRolePermissions() {
        // SUPER_ADMIN - All permissions
        for (Permission permission : Permission.values()) {
            createRolePermission(Role.SUPER_ADMIN, permission);
        }
        
        // ADMIN - All permissions
        for (Permission permission : Permission.values()) {
            createRolePermission(Role.ADMIN, permission);
        }
        
        // PRODUCT_ADMIN - Hospital and Doctor management
        createRolePermission(Role.PRODUCT_ADMIN, Permission.MANAGE_HOSPITAL);
        createRolePermission(Role.PRODUCT_ADMIN, Permission.MANAGE_DOCTOR);
        createRolePermission(Role.PRODUCT_ADMIN, Permission.VIEW_QUEUE);
        createRolePermission(Role.PRODUCT_ADMIN, Permission.VIEW_PATIENT);
        
        // HOSPITAL_ADMIN - All permissions for their hospital (can manage everything)
        for (Permission permission : Permission.values()) {
            createRolePermission(Role.HOSPITAL_ADMIN, permission);
        }
        
        // HOSPITAL_STAFF - Queue, booking, and doctor management
        createRolePermission(Role.HOSPITAL_STAFF, Permission.MANAGE_DOCTOR);
        createRolePermission(Role.HOSPITAL_STAFF, Permission.MANAGE_QUEUE);
        createRolePermission(Role.HOSPITAL_STAFF, Permission.VIEW_QUEUE);
        createRolePermission(Role.HOSPITAL_STAFF, Permission.CHANGE_QUEUE_STATUS);
        createRolePermission(Role.HOSPITAL_STAFF, Permission.VIEW_PATIENT);
        createRolePermission(Role.HOSPITAL_STAFF, Permission.MANAGE_BOOKING);
        
        // DOCTOR - View own bookings and queues
        createRolePermission(Role.DOCTOR, Permission.VIEW_QUEUE);
        createRolePermission(Role.DOCTOR, Permission.VIEW_PATIENT);
        
        // PATIENT - Manage own bookings
        createRolePermission(Role.PATIENT, Permission.MANAGE_BOOKING);
        createRolePermission(Role.PATIENT, Permission.VIEW_QUEUE);
        
        // PUBLIC - No permissions (read-only access via public endpoints)
    }
    
    private void createRolePermission(Role role, Permission permission) {
        if (!rolePermissionRepository.existsByRoleAndPermission(role, permission)) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }
}
