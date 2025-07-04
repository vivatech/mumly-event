package com.vivatech.mumly_event.dto;

import com.vivatech.mumly_event.helper.MumlyEnums;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayoutResponseDto {
    private Integer eventId;
    private String eventTitle;
    private String eventOrganizerName;
    private String eventCreatedBy;
    private Integer numberOfParticipants;
    private Double amount;
    private Double commission;
    private Double netAmount;
    private MumlyEnums.PaymentStatus paymentStatus;
}
