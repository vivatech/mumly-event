package com.vivatech.mumly_event.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationDto {
    private Integer id;

    @NotNull(message = "Participant name is required")
    private String participantName;
    @NotNull(message = "Guardian name is required")
    private String guardianName;
    @NotNull(message = "Guardian phone number is required")
    private String guardianPhone;
    @Email(message = "Invalid email address")
    private String guardianEmail;
    @Email(message = "Invalid email address")
    private String participantEmail;
    @NotNull(message = "Participant phone number is required")
    private String participantPhone;
    private String participantAddress;
    private String participantGender;
    private Double participantAge;
    private Integer eventId;
    private Integer ticketId;
}
