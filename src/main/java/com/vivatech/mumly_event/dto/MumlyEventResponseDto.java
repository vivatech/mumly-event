package com.vivatech.mumly_event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vivatech.mumly_event.model.EventCategory;
import com.vivatech.mumly_event.model.Tickets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MumlyEventResponseDto {
    private Integer id;
    private String eventName;
    private EventCategory eventCategory;
    private String description;
    private LocalDate eventDate;
    private LocalDate eventEndDate;
    private String eventTime;
    private String eventType;
    private String venueName;
    private String venueAddress;
    private List<Tickets> ticketPrice;
    private Integer totalTickets;
    private Integer soldTickets;
    private Integer availableTickets;
    private String eventCoverImage;
    private String eventBrochure;
    private String eventOrganiserName;
    private String eventOrganiserPhone;
    private String eventCreatedBy;
    private Double amount;
}
