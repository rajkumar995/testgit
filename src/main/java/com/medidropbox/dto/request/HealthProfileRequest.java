package com.medidropbox.dto.request;

import com.medidropbox.enums.BloodGroup;
import com.medidropbox.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthProfileRequest {
    
    @NotNull(message = "Height is required")
    @Min(value = 50, message = "Height must be at least 50 cm")
    @Max(value = 300, message = "Height must not exceed 300 cm")
    private Double heightCm;
    
    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;
    
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;
}

