package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mumly_event_tbl")
public class MumlyEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @NotNull(message = "Event name is required")
    private String eventName;
    @ManyToOne
    @NotNull(message = "Event category is required")
    private EventCategory eventCategory;
    private String eventDescription;
    private List<String> eventPictureList;
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    @NotNull(message = "Start time is required")
    private String startTime;
    @NotNull(message = "End time is required")
    private String endTime;
    private String timeZone;
    @NotNull(message = "Event type is required")
    private String eventType;
    private String venueName;
    private String venueAddress;
    @NotNull(message = "Organizer name is required")
    private String organizerName;

    @Email
    private String organizerContactEmail;
    @NotNull(message = "Organizer phone number is required")
    private String organizerPhoneNumber;

    private String eventCoverImage;
    private String eventPicture;
    private String eventBrochure;
    private int maximumNumberOfAttendees;
    private String specialInstructions;

    @OneToMany(orphanRemoval = true)
    private List<Tickets> tickets;

    @ManyToOne
    private MumlyEventOrganizer createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    @ManyToOne
    private MumlyEventOrganizer updatedBy;
    private String eventStatus;

}
