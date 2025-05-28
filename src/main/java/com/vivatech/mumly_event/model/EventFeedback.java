package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "temp_mumly_event_feedback")
public class EventFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Feedback checkboxes
    private Boolean performanceInActivity;
    private Boolean behavioralObservations;
    private Boolean suggestionsForImprovement;

    // Free-text feedback
    private String message;

    @ManyToOne
    @JoinColumn(name = "event_registration_id", nullable = false)
    private EventRegistration eventRegistration;

    private LocalDateTime createdAt;
}
