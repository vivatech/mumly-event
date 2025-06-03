package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTicketDetail {
    private String eventName;
    private LocalDate eventDate;
    private String eventTime;
    private String eventLocation;
    private String eventImage;
    private String referenceNumber;
    private String transactionNo;
}
