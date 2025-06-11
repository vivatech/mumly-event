package com.vivatech.mumly_event.dto;

import com.vivatech.mumly_event.model.Tickets;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MumlyEventRequestDto {
    private Integer id;
    @NotNull(message = "Event name is required")
    private String eventName;

    @NotNull(message = "Event category ID is required")
    private Integer eventCategoryId;

    private String eventDescription;

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
    @NotNull(message = "Organizer contact email is required")
    private String organizerContactEmail;

    @NotNull(message = "Organizer phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{12}$", message = "Invalid phone number format")
    private String organizerPhoneNumber;

    private String tickets;

    // Multipart files for uploads
    private MultipartFile eventCoverImageFile;
    private List<MultipartFile> eventPictureList;
    private MultipartFile eventBrochureFile;
    private MultipartFile eventPictureUpload;

    @Min(value = 1, message = "Maximum attendees must be at least 1")
    private int maximumNumberOfAttendees;

    private String specialInstructions;
    @NotNull(message = "Created by is required")
    private String createdBy;
}
