package com.medidropbox.dto.request;

import com.medidropbox.enums.QueueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusUpdateRequest {
    @NotNull(message = "Queue status is required")
    private QueueStatus status;
}
