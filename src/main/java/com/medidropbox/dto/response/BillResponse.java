package com.medidropbox.dto.response;

import com.medidropbox.enums.BillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {

    private Long id;
    private Long hospitalId;
    private Long hospitalPatientId;
    private String patientName;
    private String patientPhone;
    private BillStatus status;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDateTime createdAt;

    @Builder.Default
    private List<BillItemResponse> items = new ArrayList<>();
}
