package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.EventFeedbackRequestDto;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.EventFeedback;
import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.notification.NotificationService;
import com.vivatech.mumly_event.repository.EventFeedbackRepository;
import com.vivatech.mumly_event.repository.EventRegistrationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/event/feedback")
public class EventFeedbackController {
    private final EventFeedbackRepository feedbackRepository;
    private final EventRegistrationRepository registrationRepository;
    private final NotificationService notificationService;

    public EventFeedbackController(EventFeedbackRepository feedbackRepository,
                                   EventRegistrationRepository registrationRepository,
                                   NotificationService notificationService) {
        this.feedbackRepository = feedbackRepository;
        this.registrationRepository = registrationRepository;
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<String> submitFeedback(@RequestBody EventFeedbackRequestDto dto) {
        Optional<EventRegistration> registrationOpt = registrationRepository.findById(dto.getEventRegistrationId());

        if (registrationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found");
        }

        EventFeedback feedback = new EventFeedback();
        feedback.setPerformanceInActivity(dto.isPerformanceInActivity());
        feedback.setBehavioralObservations(dto.isBehavioralObservations());
        feedback.setSuggestionsForImprovement(dto.isSuggestionsForImprovement());
        feedback.setMessage(dto.getMessage());
        feedback.setEventRegistration(registrationOpt.get());
        feedback.setCreatedAt(LocalDateTime.now());

        EventFeedback savedFeedback = feedbackRepository.save(feedback);

        notificationService.sendAdminNotification(savedFeedback.getId(), MumlyEnums.NotificationType.FEEDBACK, savedFeedback.getMessage());

        return ResponseEntity.ok("Feedback submitted successfully");
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<List<EventFeedback>> getFeedbacks(@PathVariable Integer registrationId) {
        List<EventFeedback> feedbackList = feedbackRepository.findByEventRegistrationId(registrationId);
        return ResponseEntity.ok(feedbackList);
    }
}
