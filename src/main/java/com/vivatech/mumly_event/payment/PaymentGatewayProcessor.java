package com.vivatech.mumly_event.payment;

import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.MumlyEnums;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class PaymentGatewayProcessor {
    private final List<PaymentInterface> sortedProcessors;

    public PaymentGatewayProcessor(List<PaymentInterface> sortedProcessors) {
        this.sortedProcessors = sortedProcessors;
    }

    public PaymentInterface getMatchedProcessor(MumlyEnums.PaymentMode paymentMode) {
        for (PaymentInterface processor : sortedProcessors) {
            if(processor.supports(paymentMode)) {
                return processor;
            }
        }
        return null;
    }

    public Response sendPayment(PaymentDto paymentDto, MumlyEnums.PaymentMode paymentMode){
        PaymentInterface matchedProcessor = getMatchedProcessor(paymentMode);
        return matchedProcessor.sendPayment(paymentDto);
    }

    public Response refundPayment(PaymentDto dto, MumlyEnums.PaymentMode paymentMode){
        PaymentInterface matchedProcessor = getMatchedProcessor(paymentMode);
        return matchedProcessor.reversePayment(dto);
    }

    public MumlyEnums.PaymentAggregator getPaymentAggregator(String country) {
        HashMap<String, MumlyEnums.PaymentAggregator> map = new HashMap<>();
        map.put("SO", MumlyEnums.PaymentAggregator.WAAFI);
        map.put("KE", MumlyEnums.PaymentAggregator.SAFARI);
        return map.get(country);
    }
}
