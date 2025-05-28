package com.vivatech.mumly_event.dto;

import lombok.Data;

@Data
public class AttendanceDto {
    private Integer eventRegistrationId;
    private Boolean present;
}
