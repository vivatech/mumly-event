package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceStatus {
    private LocalDate date;
    private int presentCount;
    private int absentCount;
}
