package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventFeedbackRequestDto {
    private boolean performanceInActivity;
    private boolean behavioralObservations;
    private boolean suggestionsForImprovement;
    private String message;
    private Integer eventRegistrationId;
}
