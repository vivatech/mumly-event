package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventReportResponse {
    private Double feedBackSummary;
    private Integer totalParticipants;
    private Map<String, Integer> participantNoByMonth;
    private Map<String, AttendanceStatus> attendanceByMonth;
}
