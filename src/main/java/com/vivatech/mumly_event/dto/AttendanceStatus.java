package com.vivatech.mumly_event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class AttendanceStatus {
    private LocalDate date;
    private String month;
    private int presentCount;
    private int absentCount;
    private String name;
    private String email;
}
