package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.AttendanceDto;
import com.vivatech.mumly_event.dto.AttendanceRequestDto;
import com.vivatech.mumly_event.dto.AttendanceStatus;
import com.vivatech.mumly_event.dto.AttendanceSummaryDto;
import com.vivatech.mumly_event.model.Attendance;
import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.repository.AttendanceRepository;
import com.vivatech.mumly_event.repository.EventRegistrationRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/event/attendance")
public class AttendanceController {

    private final EventRegistrationRepository eventRegistrationRepository;
    private final AttendanceRepository attendanceRepository;

    public AttendanceController(EventRegistrationRepository eventRegistrationRepository,
                                AttendanceRepository attendanceRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitAttendance(@RequestBody AttendanceRequestDto request) {
        for (AttendanceDto dto : request.getAttendance()) {
            Optional<EventRegistration> registrationOpt = eventRegistrationRepository.findById(dto.getEventRegistrationId());
            if (registrationOpt.isPresent()) {
                Attendance attendance = new Attendance();
                attendance.setDate(request.getDate());
                attendance.setPresent(dto.getPresent());
                attendance.setEventRegistration(registrationOpt.get());
                attendanceRepository.save(attendance);
            }
        }
        return ResponseEntity.ok("Attendance submitted successfully");
    }

    @GetMapping("/history/summary")
    public AttendanceSummaryDto getAttendanceSummary(@RequestParam Integer participantId,
                                                     @RequestParam LocalDate startDate,
                                                     @RequestParam LocalDate endDate) {
        List<Object[]> results = attendanceRepository.getAttendanceSummary(participantId, startDate, endDate);
        List<AttendanceStatus> statusList = results.stream()
                .map(obj -> AttendanceStatus.builder()
                        .date((LocalDate) obj[0])
                        .presentCount(((Number) obj[1]).intValue())
                        .absentCount(((Number) obj[2]).intValue())
                        .build()).toList();

        //Calculating total present and total absent
        List<Attendance> attendances = attendanceRepository.findByEventRegistrationId(participantId);
        int totalPresent = attendances.stream().mapToInt(ele -> ele.getPresent() ? 1 : 0).sum();
        int totalAbsent = attendances.stream().mapToInt(ele -> !ele.getPresent() ? 1 : 0).sum();

        return new AttendanceSummaryDto(totalPresent, totalAbsent, statusList);
    }

    @GetMapping("/history")
    public List<AttendanceStatus> getAttendanceHistoryNew(@RequestParam Integer eventId,
                                                       @RequestParam LocalDate startDate,
                                                       @RequestParam LocalDate endDate,
                                                       @RequestParam(required = false) String participantName) {
        List<EventRegistration> registrationList = eventRegistrationRepository.findBySelectedEventIdIn(List.of(eventId));
        List<Attendance> attendanceList = attendanceRepository.findByEventRegistrationIn(registrationList);
        List<AttendanceStatus> statusList = attendanceList.stream()
                .filter(ele -> ele.getDate().isAfter(startDate.minusDays(1)) && ele.getDate().isBefore(endDate.plusDays(1)))
                .map(obj -> AttendanceStatus.builder()
                .date(obj.getDate())
                .presentCount(obj.getPresent() ? 1 : 0)
                .absentCount(obj.getPresent() ? 0 : 1)
                .name(obj.getEventRegistration().getParticipantName())
                .email(obj.getEventRegistration().getParticipantEmail())
                .build()).toList();
        if (!StringUtils.isEmpty(participantName)) {
            String lowerCaseName = participantName.toLowerCase();
            statusList = statusList.stream()
                    .filter(ele -> ele.getName().toLowerCase().contains(lowerCaseName))
                    .toList();
        }
        return statusList;
    }
}
