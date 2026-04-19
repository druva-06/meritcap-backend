package com.meritcap.DTOs.requestDTOs.lead;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReassignLeadRequestDto {

    @NotNull(message = "New counselor ID is required")
    private Long newCounselorId;

    private String reason;
}
