package com.vivatech.mumly_event.payment;

import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.helper.MumlyUtils;
import com.vivatech.mumly_event.repository.MumlyEventPaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CashPayment implements PaymentInterface {

    @Autowired
    private MumlyEventPaymentRepository paymentRepository;

    @Override
    public boolean supports(MumlyEnums.PaymentMode paymentMode) {
        return paymentMode.equals(MumlyEnums.PaymentMode.CASH);
    }

    @Override
    public Response sendPayment(PaymentDto paymentDto) {
        return Response.builder().status(MumlyEnums.PaymentStatus.SUCCESS.toString()).data(MumlyUtils.generateRandomString()).build();
    }

    @Override
    public Response reversePayment(PaymentDto dto) {
        return Response.builder().status(MumlyEnums.PaymentStatus.SUCCESS.toString()).message("Payment reversed successfully").data(MumlyUtils.generateRandomString()).build();
    }
}
