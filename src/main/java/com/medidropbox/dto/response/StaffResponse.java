package com.medidropbox.dto.response;

import com.medidropbox.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffResponse {
    private Long id;
    private String username;
    private Role role;
    private String name;
    private Boolean isActive;
    private Long hospitalId;
    private String hospitalName;
}
