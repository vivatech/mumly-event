package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.EventReportResponse;
import com.vivatech.mumly_event.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@RequestMapping("/api/v1/event/report")
public class EventReportController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public EventReportResponse displayFeedBackPoints(@RequestParam String username,
                                                     @RequestParam(required = false,
                                                             defaultValue = "#{T(java.time.Year).now().getValue()}") Integer year) {
        EventReportResponse reportResponse = EventReportResponse.builder()
                .feedBackSummary(dashboardService.getAverageFeedbackParent(username))
                .totalParticipants(dashboardService.getNumberOfParticipantOfEventOrganiser(username))
                .participantNoByMonth(dashboardService.getParticipantCountByMonth(username, year))
                .attendanceByMonth(dashboardService.getAttendanceByMonth(username, year))
                .mumlyEventList(dashboardService.getTop10EventByDate(username))
                .build();
        reportResponse.setAttendanceRate(dashboardService.attendanceRate(reportResponse.getAttendanceByMonth()));
        return reportResponse;
    }


}
