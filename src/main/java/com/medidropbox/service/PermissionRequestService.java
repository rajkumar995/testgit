package com.medidropbox.service;

import com.medidropbox.dto.request.CreatePermissionRequestRequest;
import com.medidropbox.dto.request.GrantReportPermissionRequest;
import com.medidropbox.dto.response.PermissionRequestResponse;
import com.medidropbox.dto.response.PermissionResponse;

import java.util.List;

/**
 * Service for doctor/hospital permission requests (reports, vitals, health).
 * Doctor/hospital raises request; patient approves/rejects. When approved, permission is created.
 * Unified API for all permission management (requests + granted permissions).
 */
public interface PermissionRequestService {

    /**
     * Create a permission request (doctor/hospital side).
     * @param hospitalId Requester's hospital (from token)
     * @param doctorId Optional doctor ID (null for hospital-level)
     * @param request globalPatientId, scope, notes
     */
    PermissionRequestResponse create(Long hospitalId, Long doctorId, CreatePermissionRequestRequest request);

    /**
     * List all requests made by this hospital/doctor (my requests).
     */
    List<PermissionRequestResponse> listMyRequests(Long doctorId, Long hospitalId);

    /**
     * List requests for a specific patient (for hospital viewing this patient's request history).
     */
    List<PermissionRequestResponse> listForPatient(Long globalPatientId, Long doctorId, Long hospitalId);

    /**
     * List all requests for the authenticated patient (patient side).
     */
    List<PermissionRequestResponse> listForAuthenticatedPatient(Long globalPatientId);

    /**
     * Patient approves a request. Creates permission (ReportPermission/VitalsPermission/HealthPermission).
     */
    PermissionRequestResponse approve(Long requestId, Long globalPatientId);

    /**
     * Patient rejects a request.
     */
    PermissionRequestResponse reject(Long requestId, Long globalPatientId);

    /**
     * Patient revokes an approved request (revokes the granted permission).
     */
    PermissionRequestResponse revoke(Long requestId, Long globalPatientId);

    /**
     * Doctor/hospital cancels their own pending request.
     */
    PermissionRequestResponse cancel(Long requestId, Long hospitalId, Long doctorId);

    /**
     * Get all granted permissions for patient (all scopes: REPORTS, VITALS, HEALTH).
     */
    List<PermissionResponse> getAllGrantedPermissions(Long globalPatientId);

    /**
     * Grant report permission directly (without request flow).
     */
    PermissionResponse grantReportPermission(Long globalPatientId, GrantReportPermissionRequest request);

    /**
     * Revoke any permission by permission ID and scope.
     */
    void revokePermission(Long permissionId, String scope, Long globalPatientId);
}
