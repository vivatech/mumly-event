package com.vivatech.mumly_event.service;

import com.vivatech.mumly_event.dto.AttendanceStatus;
import com.vivatech.mumly_event.dto.EventDashboardHistory;
import com.vivatech.mumly_event.dto.MumlyEventResponseDto;
import com.vivatech.mumly_event.dto.PayoutMetricsDto;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
public class DashboardService {
    @Autowired
    private MumlyEventPaymentRepository mumlyEventPaymentRepository;
    @Autowired
    private MumlyEventPayoutRepository mumlyEventPayoutRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;
    @Autowired
    private MumlyEventOrganizerRepository mumlyEventOrganizerRepository;
    @Autowired
    private MumlyEventRepository mumlyEventRepository;
    @Autowired
    private MumlyAdminsRepository mumlyAdminsRepository;

    @Autowired
    private ParentFeedbackRepository parentFeedbackRepository;

    public List<MumlyEventResponseDto> getTop10EventByDate(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        LocalDate oneMonthBefore = LocalDate.now().plusMonths(1);
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedByAndStartDateGreaterThanEqual(organizer, oneMonthBefore);

        List<MumlyEventResponseDto> response = new ArrayList<>();
        for (MumlyEvent event : eventList) {
            MumlyEventResponseDto responseDto = new MumlyEventResponseDto();
            responseDto.setEventName(event.getEventName());
            responseDto.setEventCategory(event.getEventCategory());
            responseDto.setEventDate(event.getStartDate());
            responseDto.setAmount(event.getTickets().stream().map(Tickets::getTicketPrice).findFirst().orElse(0.0));
            responseDto.setStatus(event.getEventStatus());
            responseDto.setEventCoverImage(EventConstants.EVENT_COVER_PICTURE + event.getId() + "/" + event.getEventCoverImage());
            response.add(responseDto);
        }
        return response;
    }

    public Double getAverageFeedbackParent(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        List<Integer> eventIds = eventList.stream().map(MumlyEvent::getId).distinct().toList();
        Double averageRatingByEventId = parentFeedbackRepository.findAverageRatingByEventId(eventIds);
        return averageRatingByEventId != null ? averageRatingByEventId : 0.0;
    }

    public Integer getNumberOfParticipantOfEventOrganiser(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        return eventRegistrationRepository.countBySelectedEventIn(eventList);
    }

    public Map<String, Integer> getParticipantCountByMonth(String username, Integer year) {
        // Default to current year if not provided
        int targetYear = (year != null) ? year : Year.now().getValue();

        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository
                .findByAdminId(mumlyAdmin.getId())
                .orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));

        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());

        if (eventList.isEmpty()) {
            return initializeEmptyMonthMap(); // Return all months with 0
        }

        List<Integer> eventIds = eventList.stream()
                .map(MumlyEvent::getId)
                .toList();

        List<EventRegistration> registrations = eventRegistrationRepository.findBySelectedEventIdIn(eventIds);

        // Initialize all months with 0
        Map<String, Integer> participantCountByMonth = initializeEmptyMonthMap();

        for (EventRegistration reg : registrations) {
            if (reg.getCreatedAt() != null && reg.getCreatedAt().getYear() == targetYear) {
                String month = reg.getCreatedAt().getMonth()
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Jan", "Feb", etc.

                participantCountByMonth.put(month, participantCountByMonth.get(month) + 1);
            }
        }

        return participantCountByMonth;
    }

    public Map<String, AttendanceStatus> getAttendanceByMonth(String username, Integer year) {
        // Default to current year if not provided
        int targetYear = (year != null) ? year : Year.now().getValue();

        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository
                .findByAdminId(mumlyAdmin.getId())
                .orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));

        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());

        if (eventList.isEmpty()) {
            return initializeEmptyAttendanceMap(); // Return all months with 0
        }

        List<Integer> eventIds = eventList.stream()
                .map(MumlyEvent::getId)
                .toList();

        List<EventRegistration> registrations = eventRegistrationRepository.findBySelectedEventIdIn(eventIds);

        List<Attendance> attendances = attendanceRepository.findByEventRegistrationIn(registrations);

        Map<String, AttendanceStatus> attendanceMap = initializeEmptyAttendanceMap();

        for (Attendance att : attendances) {
            if (att.getDate() != null && att.getDate().getYear() == targetYear) {
                String month = att.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                AttendanceStatus status = attendanceMap.get(month);

                if (Boolean.TRUE.equals(att.getPresent())) {
                    status.setPresentCount(status.getPresentCount() + 1);
                } else {
                    status.setAbsentCount(status.getAbsentCount() + 1);
                }
            }
        }

        return attendanceMap;
    }

    public Double attendanceRate(Map<String, AttendanceStatus> attendanceMap) {
        // Calculate the overall attendance rate
        double totalPresent = attendanceMap.values().stream().mapToDouble(AttendanceStatus::getPresentCount).sum();
        double totalAbsent = attendanceMap.values().stream().mapToDouble(AttendanceStatus::getAbsentCount).sum();
        double totalAttendances = totalPresent + totalAbsent;
        return totalAttendances <= 0 ? 0.0 : totalPresent / totalAttendances * 100;
    }

    private Map<String, Integer> initializeEmptyMonthMap() {
        Map<String, Integer> emptyMap = new LinkedHashMap<>();
        emptyMap.put("Jan", 0);
        emptyMap.put("Feb", 0);
        emptyMap.put("Mar", 0);
        emptyMap.put("Apr", 0);
        emptyMap.put("May", 0);
        emptyMap.put("Jun", 0);
        emptyMap.put("Jul", 0);
        emptyMap.put("Aug", 0);
        emptyMap.put("Sep", 0);
        emptyMap.put("Oct", 0);
        emptyMap.put("Nov", 0);
        emptyMap.put("Dec", 0);
        return emptyMap;
    }

    private Map<String, AttendanceStatus> initializeEmptyAttendanceMap() {
        Map<String, AttendanceStatus> map = new LinkedHashMap<>();
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            map.put(monthName, AttendanceStatus.builder().month(monthName).presentCount(0).absentCount(0).build());
        }
        return map;
    }

    public Double calculateGrossRevenue(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        double grossAmount = 0;
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventIdAndPaymentStatusIn(event.getId(), Collections.singletonList(MumlyEnums.PaymentStatus.SUCCESS.toString()));
            if (mumlyEventPayout == null) continue;
            grossAmount += mumlyEventPayout.getAmount();
        }
        return grossAmount;
    }

    public Double calculateNetRevenue(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        double netAmount = 0;
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventIdAndPaymentStatusIn(event.getId(), Collections.singletonList(MumlyEnums.PaymentStatus.SUCCESS.toString()));
            if (mumlyEventPayout == null) continue;
            netAmount += mumlyEventPayout.getNetAmount();
        }
        return netAmount;
    }

    public List<Triple<String, Double, Double>> revenueByEventCategory(String username, Double netRevenue) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        Map<String, Double> revenueByEventCategory = new HashMap<>();
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventIdAndPaymentStatusIn(event.getId(), Collections.singletonList(MumlyEnums.PaymentStatus.SUCCESS.toString()));
            if (mumlyEventPayout == null) continue;
            double netAmount = mumlyEventPayout.getNetAmount();
            revenueByEventCategory.put(event.getEventCategory().getName(), revenueByEventCategory.getOrDefault(event.getEventCategory().getName(), 0.0) + netAmount);
        }
        List<Triple<String, Double, Double>> data = new ArrayList<>();
        for (Map.Entry<String, Double> entry : revenueByEventCategory.entrySet()) {
            data.add(Triple.of(entry.getKey(), entry.getValue(), (entry.getValue() / netRevenue) * 100));
        }
        return data;

    }

    public Map<String, Integer> revenueByMonth(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        int targetYear = Year.now().getValue();
        List<MumlyEvent> mumlyEvents = mumlyEventRepository.findByCreatedById(organizer.getId());
        List<MumlyEvent> eventList = mumlyEvents.stream().filter(ele -> ele.getStartDate().getYear() == targetYear).toList();
        Map<String, Integer> revenueByMonth = initializeEmptyMonthMap();
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventIdAndPaymentStatusIn(event.getId(), Collections.singletonList(MumlyEnums.PaymentStatus.SUCCESS.toString()));
            if (mumlyEventPayout == null) continue;
            double netAmount = mumlyEventPayout.getNetAmount();
            String month = event.getStartDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Jan", "Feb", etc.
            revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0) + (int) netAmount);
        }
        return revenueByMonth;
    }

    public Map<String, Integer> grossRevenueByMonth(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        int targetYear = Year.now().getValue();
        List<MumlyEvent> mumlyEvents = mumlyEventRepository.findByCreatedById(organizer.getId());
        List<MumlyEvent> eventList = mumlyEvents.stream().filter(ele -> ele.getStartDate().getYear() == targetYear).toList();
        Map<String, Integer> grossRevenueByMonth = initializeEmptyMonthMap();
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventIdAndPaymentStatusIn(event.getId(), Collections.singletonList(MumlyEnums.PaymentStatus.SUCCESS.toString()));
            if (mumlyEventPayout == null) continue;
            double grossAmount = mumlyEventPayout.getAmount();
            String month = event.getStartDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Jan", "Feb", etc.
            grossRevenueByMonth.put(month, grossRevenueByMonth.getOrDefault(month, 0) + (int) grossAmount);
        }
        return grossRevenueByMonth;
    }

    public PayoutMetricsDto calculatePayoutMetrics(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        PayoutMetricsDto payoutMetricsDto = new PayoutMetricsDto();

        int totalPayout = 0;
        int pendingPayout = 0;
        double commission = 0.0;

        List<MumlyEventPayout> payoutList = new ArrayList<>();
        for (MumlyEvent event : eventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventId(event.getId());
            if (mumlyEventPayout == null) continue;
            if (mumlyEventPayout.getPaymentStatus().equals(MumlyEnums.PaymentStatus.PENDING.toString())) pendingPayout += 1;
            if (mumlyEventPayout.getPaymentStatus().equals(MumlyEnums.PaymentStatus.SUCCESS.toString())) totalPayout += 1;
            commission += mumlyEventPayout.getCommission();
            payoutList.add(mumlyEventPayout);
        }
        payoutMetricsDto.setTotalPayout(totalPayout);
        payoutMetricsDto.setPendingPayout(pendingPayout);
        payoutMetricsDto.setCommission(commission);
        LocalDate nextPaymentDate = payoutList.stream()
                .filter(ele -> ele.getPaymentStatus().equalsIgnoreCase(MumlyEnums.PaymentStatus.PENDING.toString()))
                .sorted(Comparator.comparing(ele -> ele.getEvent().getEndDate()))
                .findFirst()
                .map(ele -> ele.getEvent().getEndDate()).orElse(null);
        payoutMetricsDto.setNextPayoutDate(nextPaymentDate);
        return payoutMetricsDto;
    }

    public EventDashboardHistory eventDashboardHistory(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        List<MumlyEventPayout> payoutList = mumlyEventPayoutRepository.findByEventIn(eventList);

        int hostedEvent = 0;
        int publishedEvent = 0;
        EventDashboardHistory eventDashboardHistory = new EventDashboardHistory();
        for (MumlyEventPayout payout : payoutList) {
            if (payout.getPaymentStatus().equals(MumlyEnums.PaymentStatus.SUCCESS.toString())) hostedEvent += 1;
            if (payout.getPaymentStatus().equals(MumlyEnums.PaymentStatus.PENDING.toString())) publishedEvent += 1;
        }
        int draftEvent = (int) eventList.stream()
                .filter(ele -> ele.getEventStatus().equalsIgnoreCase(MumlyEnums.EventStatus.PENDING.toString()))
                .count();
        eventDashboardHistory.setHostedEvent(hostedEvent);
        eventDashboardHistory.setPublishedEvent(publishedEvent);
        eventDashboardHistory.setDraftEvent(draftEvent);
        return eventDashboardHistory;
    }

    public Integer refundIssued(String username) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<MumlyEvent> eventList = mumlyEventRepository.findByCreatedById(organizer.getId());
        int refundCount = 0;
        for (MumlyEvent event : eventList) {
            List<MumlyEventPayment> paymentList = mumlyEventPaymentRepository.findByEventRegistrationSelectedEventIdAndPaymentStatus(event.getId(), MumlyEnums.PaymentStatus.REFUND.toString());
            refundCount += paymentList.size();
        }
        return refundCount;
    }


}
