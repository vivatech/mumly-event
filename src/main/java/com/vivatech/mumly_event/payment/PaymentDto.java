package com.vivatech.mumly_event.payment;

import com.vivatech.mumly_event.helper.MumlyEnums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDto {
    private String msisdn;
    private Double amount;
    private String transactionId;
    private String referenceNo;
    private MumlyEnums.PaymentMode paymentMode;
    private Integer eventRegistrationId;
    private String reason;

}
