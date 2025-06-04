package com.vivatech.mumly_event.service;

import com.vivatech.mumly_event.dto.AttendanceStatus;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
public class DashboardService {
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



}
