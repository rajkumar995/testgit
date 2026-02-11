package com.medidropbox.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillRequest {

    @NotNull(message = "Hospital patient (customer) is required")
    private Long hospitalPatientId;

    private String notes;

    @Valid
    private List<BillItemRequest> items = new ArrayList<>();
}
