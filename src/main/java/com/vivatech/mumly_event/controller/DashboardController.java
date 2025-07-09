package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.EventReportResponse;
import com.vivatech.mumly_event.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/event/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public EventReportResponse displayDashboardData(@RequestParam String username) {
        EventReportResponse response = EventReportResponse.builder()
                .grossRevenue(dashboardService.calculateGrossRevenue(username))
                .netRevenue(dashboardService.calculateNetRevenue(username))
                .revenueByMonth(dashboardService.revenueByMonth(username))
                .grossRevenueByMonth(dashboardService.grossRevenueByMonth(username))
                .feedBackSummary(dashboardService.getAverageFeedbackParent(username))
                .payoutMetrics(dashboardService.calculatePayoutMetrics(username))
                .eventHistoryTally(dashboardService.eventDashboardHistory(username))
                .mumlyEventList(dashboardService.getTop10EventByDate(username))
                .refundIssued(dashboardService.refundIssued(username))
                .build();
        response.setRevenueByCategory(dashboardService.revenueByEventCategory(username, response.getNetRevenue()));
        return response;
    }
}
