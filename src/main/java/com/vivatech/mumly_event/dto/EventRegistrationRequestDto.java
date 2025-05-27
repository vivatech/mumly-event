package com.vivatech.mumly_event.dto;

import com.vivatech.mumly_event.payment.PaymentDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationRequestDto {
    private EventRegistrationDto registrationDto;
    private PaymentDto paymentDto;
}
