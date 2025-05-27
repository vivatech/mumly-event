package com.vivatech.mumly_event.payment;

import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.MumlyEnums;

public interface PaymentInterface {
    boolean supports(MumlyEnums.PaymentMode paymentMode);

    Response sendPayment(PaymentDto paymentDto);

    Response reversePayment(String msisdn, String transactionId);
}
