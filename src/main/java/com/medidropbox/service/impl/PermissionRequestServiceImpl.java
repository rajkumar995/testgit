package com.medidropbox.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.medidropbox.dto.request.CreatePermissionRequestRequest;
import com.medidropbox.dto.request.GrantReportPermissionRequest;
import com.medidropbox.dto.response.PermissionRequestResponse;
import com.medidropbox.dto.response.PermissionResponse;
import com.medidropbox.entity.HealthPermission;
import com.medidropbox.entity.PermissionRequest;
import com.medidropbox.entity.ReportPermission;
import com.medidropbox.entity.VitalsPermission;
import com.medidropbox.enums.PermissionRequestScope;
import com.medidropbox.enums.PermissionRequestStatus;
import com.medidropbox.repository.DoctorRepository;
import com.medidropbox.repository.GlobalPatientRepository;
import com.medidropbox.repository.HealthPermissionRepository;
import com.medidropbox.repository.HospitalRepository;
import com.medidropbox.repository.PermissionRequestRepository;
import com.medidropbox.repository.ReportPermissionRepository;
import com.medidropbox.repository.VitalsPermissionRepository;
import com.medidropbox.service.LabReportService;
import com.medidropbox.service.PermissionRequestService;

@Service
@Transactional
public class PermissionRequestServiceImpl implements PermissionRequestService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionRequestServiceImpl.class);

    private final PermissionRequestRepository permissionRequestRepository;
    private final ReportPermissionRepository reportPermissionRepository;
    private final HealthPermissionRepository healthPermissionRepository;
    private final VitalsPermissionRepository vitalsPermissionRepository;
    private final GlobalPatientRepository globalPatientRepository;
    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;
    private final LabReportService labReportService;

    public PermissionRequestServiceImpl(
            PermissionRequestRepository permissionRequestRepository,
            ReportPermissionRepository reportPermissionRepository,
            HealthPermissionRepository healthPermissionRepository,
            VitalsPermissionRepository vitalsPermissionRepository,
            GlobalPatientRepository globalPatientRepository,
            DoctorRepository doctorRepository,
            HospitalRepository hospitalRepository,
            LabReportService labReportService) {
        this.permissionRequestRepository = permissionRequestRepository;
        this.reportPermissionRepository = reportPermissionRepository;
        this.healthPermissionRepository = healthPermissionRepository;
        this.vitalsPermissionRepository = vitalsPermissionRepository;
        this.globalPatientRepository = globalPatientRepository;
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
        this.labReportService = labReportService;
    }

    @Override
    public PermissionRequestResponse create(Long hospitalId, Long doctorId, CreatePermissionRequestRequest request) {
        if (request.getGlobalPatientId() == null || request.getScope() == null) {
            throw new RuntimeException("Global patient ID and scope are required");
        }
        if (hospitalId == null && doctorId == null) {
            throw new RuntimeException("Either hospital or doctor must be set");
        }
        // Avoid duplicate pending for same patient/doctor/hospital/scope
        List<PermissionRequest> pending = permissionRequestRepository.findPendingByPatientAndDoctorAndHospital(
                request.getGlobalPatientId(), doctorId, hospitalId);
        boolean sameScopePending = pending.stream()
                .anyMatch(pr -> pr.getScope() == request.getScope());
        if (sameScopePending) {
            throw new RuntimeException("A pending request for this scope already exists for this patient");
        }

        PermissionRequest pr = new PermissionRequest();
        pr.setGlobalPatientId(request.getGlobalPatientId());
        pr.setDoctorId(doctorId);
        pr.setHospitalId(hospitalId);
        pr.setScope(request.getScope());
        pr.setStatus(PermissionRequestStatus.PENDING);
        pr.setRequestedAt(LocalDateTime.now());
        pr.setExpiresAt(request.getExpiresAt());
        pr.setNotes(request.getNotes());
        pr = permissionRequestRepository.save(pr);
        return mapToResponse(pr);
    }

    @Override
    public List<PermissionRequestResponse> listMyRequests(Long doctorId, Long hospitalId) {
        List<PermissionRequest> list = permissionRequestRepository.findMyRequests(doctorId, hospitalId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PermissionRequestResponse> listForPatient(Long globalPatientId, Long doctorId, Long hospitalId) {
        List<PermissionRequest> list = permissionRequestRepository.findMyRequestsForPatient(globalPatientId, doctorId, hospitalId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PermissionRequestResponse> listForAuthenticatedPatient(Long globalPatientId) {
        List<PermissionRequest> list = permissionRequestRepository.findByGlobalPatientIdOrderByRequestedAtDesc(globalPatientId);
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public PermissionRequestResponse approve(Long requestId, Long globalPatientId) {
        PermissionRequest pr = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Permission request not found"));
        if (!pr.getGlobalPatientId().equals(globalPatientId)) {
            throw new RuntimeException("You can only approve requests for your own data");
        }
        if (pr.getStatus() != PermissionRequestStatus.PENDING) {
            throw new RuntimeException("Request is no longer pending");
        }
        pr.setStatus(PermissionRequestStatus.APPROVED);
        pr.setRespondedAt(LocalDateTime.now());
        permissionRequestRepository.save(pr);

        if (pr.getScope() == PermissionRequestScope.REPORTS) {
            GrantReportPermissionRequest grant = new GrantReportPermissionRequest();
            grant.setDoctorId(pr.getDoctorId());
            grant.setHospitalId(pr.getHospitalId());
            grant.setExpiresAt(pr.getExpiresAt());
            grant.setNotes(pr.getNotes());
            labReportService.grantPermission(globalPatientId, grant);
        } else if (pr.getScope() == PermissionRequestScope.HEALTH) {
            if (healthPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                    globalPatientId, pr.getDoctorId(), pr.getHospitalId()).isEmpty()) {
                HealthPermission hp = new HealthPermission();
                hp.setGlobalPatientId(globalPatientId);
                hp.setDoctorId(pr.getDoctorId());
                hp.setHospitalId(pr.getHospitalId());
                hp.setIsActive(true);
                hp.setGrantedAt(LocalDateTime.now());
                hp.setExpiresAt(pr.getExpiresAt());
                hp.setNotes(pr.getNotes());
                healthPermissionRepository.save(hp);
            }
        } else if (pr.getScope() == PermissionRequestScope.VITALS) {
            Optional<VitalsPermission> existing = vitalsPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                    globalPatientId, pr.getDoctorId(), pr.getHospitalId());
            if (existing.isEmpty()) {
                VitalsPermission vp = new VitalsPermission();
                vp.setGlobalPatientId(globalPatientId);
                vp.setDoctorId(pr.getDoctorId());
                vp.setHospitalId(pr.getHospitalId());
                vp.setIsActive(true);
                vp.setGrantedAt(LocalDateTime.now());
                vp.setExpiresAt(pr.getExpiresAt());
                vp.setNotes(pr.getNotes());
                vitalsPermissionRepository.save(vp);
            }
        }
        return mapToResponse(pr);
    }

    @Override
    public PermissionRequestResponse reject(Long requestId, Long globalPatientId) {
        PermissionRequest pr = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Permission request not found"));
        if (!pr.getGlobalPatientId().equals(globalPatientId)) {
            throw new RuntimeException("You can only reject requests for your own data");
        }
        if (pr.getStatus() != PermissionRequestStatus.PENDING) {
            throw new RuntimeException("Request is no longer pending");
        }
        pr.setStatus(PermissionRequestStatus.REJECTED);
        pr.setRespondedAt(LocalDateTime.now());
        permissionRequestRepository.save(pr);
        return mapToResponse(pr);
    }

    @Override
    public PermissionRequestResponse cancel(Long requestId, Long hospitalId, Long doctorId) {
        PermissionRequest pr = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Permission request not found"));
        boolean isOwner = (hospitalId != null && hospitalId.equals(pr.getHospitalId()))
                || (doctorId != null && doctorId.equals(pr.getDoctorId()));
        if (!isOwner) {
            throw new RuntimeException("You can only cancel your own requests");
        }
        if (pr.getStatus() != PermissionRequestStatus.PENDING) {
            throw new RuntimeException("Only pending requests can be cancelled");
        }
        pr.setStatus(PermissionRequestStatus.CANCELLED);
        pr.setRespondedAt(LocalDateTime.now());
        permissionRequestRepository.save(pr);
        return mapToResponse(pr);
    }

    @Override
    public PermissionRequestResponse revoke(Long requestId, Long globalPatientId) {
        PermissionRequest pr = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Permission request not found"));
        if (!pr.getGlobalPatientId().equals(globalPatientId)) {
            throw new RuntimeException("You can only revoke permissions for your own data");
        }
        if (pr.getStatus() != PermissionRequestStatus.APPROVED) {
            throw new RuntimeException("Only approved requests can be revoked");
        }
        revokePermissionByScope(pr.getScope(), globalPatientId, pr.getDoctorId(), pr.getHospitalId());
        return mapToResponse(pr);
    }

    @Override
    public List<PermissionResponse> getAllGrantedPermissions(Long globalPatientId) {
        List<PermissionResponse> all = new java.util.ArrayList<>();
        // REPORTS
        List<ReportPermission> reports = reportPermissionRepository.findByGlobalPatientIdAndIsActiveTrueOrderByGrantedAtDesc(globalPatientId);
        all.addAll(reports.stream().map(rp -> mapReportPermissionToResponse(rp)).collect(Collectors.toList()));
        // VITALS
        List<VitalsPermission> vitals = vitalsPermissionRepository.findAll().stream()
                .filter(vp -> vp.getGlobalPatientId().equals(globalPatientId) && Boolean.TRUE.equals(vp.getIsActive()))
                .collect(Collectors.toList());
        all.addAll(vitals.stream().map(vp -> mapVitalsPermissionToResponse(vp)).collect(Collectors.toList()));
        // HEALTH
        List<HealthPermission> health = healthPermissionRepository.findAll().stream()
                .filter(hp -> hp.getGlobalPatientId().equals(globalPatientId) && Boolean.TRUE.equals(hp.getIsActive()))
                .collect(Collectors.toList());
        all.addAll(health.stream().map(hp -> mapHealthPermissionToResponse(hp)).collect(Collectors.toList()));
        return all;
    }

    @Override
    public PermissionResponse grantReportPermission(Long globalPatientId, GrantReportPermissionRequest request) {
        if (request.getDoctorId() == null && request.getHospitalId() == null) {
            throw new RuntimeException("Either doctorId or hospitalId must be provided");
        }
        Optional<ReportPermission> existing = reportPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                globalPatientId, request.getDoctorId(), request.getHospitalId());
        ReportPermission rp;
        if (existing.isPresent()) {
            rp = existing.get();
            rp.setIsActive(true);
            if (request.getExpiresAt() != null) rp.setExpiresAt(request.getExpiresAt());
            if (request.getNotes() != null) rp.setNotes(request.getNotes());
        } else {
            rp = new ReportPermission();
            rp.setGlobalPatientId(globalPatientId);
            rp.setDoctorId(request.getDoctorId());
            rp.setHospitalId(request.getHospitalId());
            rp.setIsActive(true);
            rp.setGrantedAt(LocalDateTime.now());
            rp.setExpiresAt(request.getExpiresAt());
            rp.setNotes(request.getNotes());
        }
        rp = reportPermissionRepository.save(rp);
        return mapReportPermissionToResponse(rp);
    }

    @Override
    public void revokePermission(Long permissionId, String scope, Long globalPatientId) {
        PermissionRequestScope scopeEnum = PermissionRequestScope.valueOf(scope.toUpperCase());
        if (scopeEnum == PermissionRequestScope.REPORTS) {
            ReportPermission rp = reportPermissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Report permission not found"));
            if (!rp.getGlobalPatientId().equals(globalPatientId)) {
                throw new RuntimeException("You can only revoke your own permissions");
            }
            rp.setIsActive(false);
            reportPermissionRepository.save(rp);
        } else if (scopeEnum == PermissionRequestScope.VITALS) {
            VitalsPermission vp = vitalsPermissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Vitals permission not found"));
            if (!vp.getGlobalPatientId().equals(globalPatientId)) {
                throw new RuntimeException("You can only revoke your own permissions");
            }
            vp.setIsActive(false);
            vitalsPermissionRepository.save(vp);
        } else if (scopeEnum == PermissionRequestScope.HEALTH) {
            HealthPermission hp = healthPermissionRepository.findById(permissionId)
                    .orElseThrow(() -> new RuntimeException("Health permission not found"));
            if (!hp.getGlobalPatientId().equals(globalPatientId)) {
                throw new RuntimeException("You can only revoke your own permissions");
            }
            hp.setIsActive(false);
            healthPermissionRepository.save(hp);
        } else {
            throw new RuntimeException("Invalid scope: " + scope);
        }
    }

    private void revokePermissionByScope(PermissionRequestScope scope, Long globalPatientId, Long doctorId, Long hospitalId) {
        if (scope == PermissionRequestScope.REPORTS) {
            Optional<ReportPermission> rp = reportPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                    globalPatientId, doctorId, hospitalId);
            if (rp.isPresent()) {
                rp.get().setIsActive(false);
                reportPermissionRepository.save(rp.get());
            }
        } else if (scope == PermissionRequestScope.VITALS) {
            Optional<VitalsPermission> vp = vitalsPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                    globalPatientId, doctorId, hospitalId);
            if (vp.isPresent()) {
                vp.get().setIsActive(false);
                vitalsPermissionRepository.save(vp.get());
            }
        } else if (scope == PermissionRequestScope.HEALTH) {
            Optional<HealthPermission> hp = healthPermissionRepository.findByGlobalPatientIdAndDoctorIdAndHospitalIdAndIsActiveTrue(
                    globalPatientId, doctorId, hospitalId);
            if (hp.isPresent()) {
                hp.get().setIsActive(false);
                healthPermissionRepository.save(hp.get());
            }
        }
    }

    private PermissionResponse mapReportPermissionToResponse(ReportPermission rp) {
        PermissionResponse.PermissionResponseBuilder builder = PermissionResponse.builder()
                .id(rp.getId())
                .patientId(rp.getGlobalPatientId())
                .scope(PermissionRequestScope.REPORTS)
                .doctorId(rp.getDoctorId())
                .hospitalId(rp.getHospitalId())
                .isActive(rp.getIsActive())
                .grantedAt(rp.getGrantedAt())
                .expiresAt(rp.getExpiresAt())
                .notes(rp.getNotes());
        if (rp.getDoctorId() != null) {
            doctorRepository.findById(rp.getDoctorId()).ifPresent(d -> builder.doctorName(d.getName()));
        }
        if (rp.getHospitalId() != null) {
            hospitalRepository.findById(rp.getHospitalId()).ifPresent(h -> builder.hospitalName(h.getName()));
        }
        return builder.build();
    }

    private PermissionResponse mapVitalsPermissionToResponse(VitalsPermission vp) {
        PermissionResponse.PermissionResponseBuilder builder = PermissionResponse.builder()
                .id(vp.getId())
                .patientId(vp.getGlobalPatientId())
                .scope(PermissionRequestScope.VITALS)
                .doctorId(vp.getDoctorId())
                .hospitalId(vp.getHospitalId())
                .isActive(vp.getIsActive())
                .grantedAt(vp.getGrantedAt())
                .expiresAt(vp.getExpiresAt())
                .notes(vp.getNotes());
        if (vp.getDoctorId() != null) {
            doctorRepository.findById(vp.getDoctorId()).ifPresent(d -> builder.doctorName(d.getName()));
        }
        if (vp.getHospitalId() != null) {
            hospitalRepository.findById(vp.getHospitalId()).ifPresent(h -> builder.hospitalName(h.getName()));
        }
        return builder.build();
    }

    private PermissionResponse mapHealthPermissionToResponse(HealthPermission hp) {
        PermissionResponse.PermissionResponseBuilder builder = PermissionResponse.builder()
                .id(hp.getId())
                .patientId(hp.getGlobalPatientId())
                .scope(PermissionRequestScope.HEALTH)
                .doctorId(hp.getDoctorId())
                .hospitalId(hp.getHospitalId())
                .isActive(hp.getIsActive())
                .grantedAt(hp.getGrantedAt())
                .expiresAt(hp.getExpiresAt())
                .notes(hp.getNotes());
        if (hp.getDoctorId() != null) {
            doctorRepository.findById(hp.getDoctorId()).ifPresent(d -> builder.doctorName(d.getName()));
        }
        if (hp.getHospitalId() != null) {
            hospitalRepository.findById(hp.getHospitalId()).ifPresent(h -> builder.hospitalName(h.getName()));
        }
        return builder.build();
    }

    private PermissionRequestResponse mapToResponse(PermissionRequest pr) {
        PermissionRequestResponse r = new PermissionRequestResponse();
        r.setId(pr.getId());
        r.setGlobalPatientId(pr.getGlobalPatientId());
        r.setDoctorId(pr.getDoctorId());
        r.setHospitalId(pr.getHospitalId());
        r.setScope(pr.getScope());
        r.setStatus(pr.getStatus());
        r.setRequestedAt(pr.getRequestedAt());
        r.setRespondedAt(pr.getRespondedAt());
        r.setExpiresAt(pr.getExpiresAt());
        r.setNotes(pr.getNotes());
        globalPatientRepository.findById(pr.getGlobalPatientId())
                .ifPresent(gp -> r.setPatientName(gp.getFullName()));
        if (pr.getDoctorId() != null) {
            doctorRepository.findById(pr.getDoctorId())
                    .ifPresent(d -> r.setDoctorName(d.getName()));
        }
        if (pr.getHospitalId() != null) {
            hospitalRepository.findById(pr.getHospitalId())
                    .ifPresent(h -> r.setHospitalName(h.getName()));
        }
        return r;
    }
}
