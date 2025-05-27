package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "temp_mumly_event_registrations")
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    private String participantName;
    private String guardianName;
    @Email(message = "Invalid email address")
    private String participantEmail;
    private String participantPhone;
    private String participantAddress;
    private String participantGender;
    private Double participantAge;
    @ManyToOne
    private MumlyEvent selectedEvent;
    @ManyToOne
    private Tickets tickets;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
}
