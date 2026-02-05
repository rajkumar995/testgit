package com.medidropbox.dto.response;

import com.medidropbox.enums.BloodGroup;
import com.medidropbox.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthProfileResponse {
    private Long id;
    private Long globalPatientId;
    private Double heightCm;
    private BloodGroup bloodGroup;
    private Gender gender;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

