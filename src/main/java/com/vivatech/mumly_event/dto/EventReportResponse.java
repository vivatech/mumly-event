package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private Double attendanceRate;
    private List<MumlyEventResponseDto> mumlyEventList = new ArrayList<>();
    //Dashboard Items
    private Double grossRevenue;
    private Double netRevenue;
    private List<Triple<String, Double, Double>> revenueByCategory = new ArrayList<>();
    private Map<String, Integer> revenueByMonth = new HashMap<>();
    private PayoutMetricsDto payoutMetrics;
    private EventDashboardHistory eventHistoryTally;
    private Integer refundIssued;
}
