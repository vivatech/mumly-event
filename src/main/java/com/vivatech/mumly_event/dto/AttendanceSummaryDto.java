package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceSummaryDto {
    private int totalPresent;
    private int totalAbsent;
    List<AttendanceStatus> attendanceStatus;
}
