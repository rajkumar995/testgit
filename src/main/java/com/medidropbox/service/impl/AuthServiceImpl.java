package com.medidropbox.service.impl;

import com.medidropbox.dto.request.LoginRequest;
import com.medidropbox.dto.request.RefreshTokenRequest;
import com.medidropbox.dto.request.ResetPasswordRequest;
import com.medidropbox.dto.response.LoginResponse;
import com.medidropbox.dto.response.UserResponse;
import com.medidropbox.entity.GlobalPatient;
import com.medidropbox.entity.User;
import com.medidropbox.enums.PatientStatus;
import com.medidropbox.enums.Role;
import com.medidropbox.repository.GlobalPatientRepository;
import com.medidropbox.repository.UserRepository;
import com.medidropbox.security.MediDropBoxJwtUtil;
import com.medidropbox.security.MediDropBoxUserDetailsService;
import com.medidropbox.service.AuthService;
import com.medidropbox.service.AuditLogService;
import com.medidropbox.service.OtpService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final GlobalPatientRepository globalPatientRepository;
    private final AuthenticationManager authenticationManager;
    private final MediDropBoxJwtUtil jwtUtil;
    private final MediDropBoxUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final AuditLogService auditLogService;
    
    public AuthServiceImpl(UserRepository userRepository,
                          GlobalPatientRepository globalPatientRepository,
                          AuthenticationManager authenticationManager,
                          MediDropBoxJwtUtil jwtUtil,
                          MediDropBoxUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder,
                          OtpService otpService,
                          AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.globalPatientRepository = globalPatientRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.auditLogService = auditLogService;
    }
    
    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<String> permissions = userDetailsService.getPermissions(user.getRole());
        // Extract hospitalId from user entity if hospital is associated
        Long hospitalId = (user.getHospital() != null) ? user.getHospital().getId() : null;
        String accessToken = jwtUtil.generateAccessToken(userDetails, user.getRole(), permissions, hospitalId);
        String refreshToken = jwtUtil.generateRefreshToken(request.getUsername());
        
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        user.setIsFirstLogin(false);
        userRepository.save(user);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }
    
    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String username = jwtUtil.extractUsername(request.getRefreshToken());
        
        if (username == null || jwtUtil.isTokenExpiredPublic(request.getRefreshToken())) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<String> permissions = userDetailsService.getPermissions(user.getRole());
        // Extract hospitalId from user entity if hospital is associated
        Long hospitalId = (user.getHospital() != null) ? user.getHospital().getId() : null;
        String accessToken = jwtUtil.generateAccessToken(userDetails, user.getRole(), permissions, hospitalId);
        String refreshToken = jwtUtil.generateRefreshToken(username);
        
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }
    
    @Override
    public LoginResponse loginWithOtp(String phone, String reqId, String otp) {
        try {
            // Verify OTP (verifyOtp + verifyAccessToken via Widget API)
            boolean otpValid = otpService.verifyOtp(reqId, otp);
            
            if (!otpValid) {
                // AUDIT: Log failed OTP authentication
                auditLogService.logAuthentication(phone, false, null);
                throw new RuntimeException("Invalid OTP");
            }
            
            // Check if patient exists (CLAIMED or UNCLAIMED)
            Optional<GlobalPatient> globalPatientOpt = globalPatientRepository.findByPhone(phone);
            
            if (globalPatientOpt.isEmpty()) {
                auditLogService.logAuthentication(phone, false, null);
                throw new RuntimeException("Patient not found. Please register first.");
            }
            
            GlobalPatient globalPatient = globalPatientOpt.get();
            
            // If UNCLAIMED, claim it
            if (globalPatient.getStatus() == PatientStatus.UNCLAIMED) {
                globalPatient.setStatus(PatientStatus.CLAIMED);
                globalPatient = globalPatientRepository.save(globalPatient);
            }
            
            // Get or create User account
            User user = userRepository.findByUsername(phone).orElse(null);
            if (user == null) {
                user = new User();
                user.setUsername(phone);
                // Dummy password - OTP is used for authentication
                user.setPassword(passwordEncoder.encode("OTP_AUTH_" + phone));
                user.setRole(Role.PATIENT);
                user.setGlobalPatient(globalPatient);
                user.setIsActive(true);
                user.setIsFirstLogin(true);
                user = userRepository.save(user);
            } else {
                // Update last login
                user.setLastLoginAt(LocalDateTime.now());
                user.setIsFirstLogin(false);
                user = userRepository.save(user);
            }
            
            // Generate tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(phone);
            List<String> permissions = userDetailsService.getPermissions(user.getRole());
            // Extract hospitalId from user entity if hospital is associated (usually null for PATIENT role)
            Long hospitalId = (user.getHospital() != null) ? user.getHospital().getId() : null;
            String accessToken = jwtUtil.generateAccessToken(userDetails, user.getRole(), permissions, hospitalId);
            String refreshToken = jwtUtil.generateRefreshToken(phone);
            
            // AUDIT: Log successful OTP authentication
            auditLogService.logAuthentication(phone, true, null);
            
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .role(user.getRole())
                    .isFirstLogin(user.getIsFirstLogin())
                    .build();
        } catch (Exception e) {
            // AUDIT: Log failed authentication
            auditLogService.logAuthentication(phone, false, null);
            throw e;
        }
    }
    
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .isFirstLogin(user.getIsFirstLogin())
                .lastLoginAt(user.getLastLoginAt())
                .hospitalId(user.getHospital() != null ? user.getHospital().getId() : null)
                .hospitalName(user.getHospital() != null ? user.getHospital().getName() : null)
                .globalPatientId(user.getGlobalPatient() != null ? user.getGlobalPatient().getId() : null)
                .build();
    }
}
