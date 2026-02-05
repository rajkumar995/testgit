package com.medidropbox.dto.request;

import com.medidropbox.enums.FileFormat;
import com.medidropbox.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LabReportUploadRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "File is required")
    private MultipartFile file;
    
    @NotNull(message = "Report date is required")
    private LocalDate reportDate;
    
    private String doctorName;
    
    private String labName;
    
    private String aiSummary;
    
    private String notes;
}
