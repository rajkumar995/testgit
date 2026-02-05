package com.medidropbox.dto.response;

import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private Role role;
    private Boolean isActive;
    private Boolean isFirstLogin;
    private LocalDateTime lastLoginAt;
    private Long hospitalId;
    private String hospitalName;
    private Long globalPatientId;
}

