package com.vivatech.mumly_event.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminNotificationDto {
    private Integer eventId;
    @NotNull(message = "Message is required")
    private String message;
    private List<Integer> participantIds = new ArrayList<>();

    public String validate() {
        StringBuilder validationErrors = new StringBuilder();

        validateRequiredField("Message", message, validationErrors);
        return validationErrors.length() <= 0 ? null : validationErrors.toString().trim();
    }

    private void validateRequiredField(String fieldName, String value, StringBuilder validationErrors) {
        if (value == null || value.trim().isEmpty()) {
            validationErrors.append(fieldName).append(" is required.");
        }
    }

}
