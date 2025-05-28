package com.vivatech.mumly_event.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AttendanceRequestDto {
    private LocalDate date;
    private List<AttendanceDto> attendance;
}
