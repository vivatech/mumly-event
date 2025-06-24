package com.vivatech.mumly_event.scheduler;
import java.time.LocalDateTime;

import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import com.vivatech.mumly_event.model.MumlyEventPayout;
import com.vivatech.mumly_event.repository.MumlyEventPaymentRepository;
import com.vivatech.mumly_event.repository.MumlyEventPayoutRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MumlyEventScheduler {

    @Autowired
    private MumlyEventPayoutRepository mumlyEventPayoutRepository;
    @Autowired
    private MumlyEventRepository mumlyEventRepository;
    @Autowired
    private MumlyEventPaymentRepository mumlyEventPaymentRepository;

    @Scheduled(cron = "0 */1 * * * *")
    private void sumAllEventPayout() {
        log.info("Executing sumAllEventPayout at {}", LocalDateTime.now());
        List<MumlyEvent> mumlyEventList = mumlyEventRepository.findByEndDateGreaterThanEqual(LocalDate.now());
        for (MumlyEvent event : mumlyEventList) {
            MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository
                    .findByEventIdAndPaymentStatusIn(
                            event.getId(),
                            Arrays.asList(
                                    MumlyEnums.PaymentStatus.PENDING.toString(),
                                    MumlyEnums.PaymentStatus.FAILED.toString()
                            )
                    );
            if (mumlyEventPayout != null) {
                List<MumlyEventPayment> paymentList = mumlyEventPaymentRepository.findByEventRegistrationSelectedEventIdAndPaymentStatus(mumlyEventPayout.getEvent().getId(), MumlyEnums.PaymentStatus.COMPLETE.toString());
                if (paymentList.isEmpty()) continue;
                Map<MumlyEnums.PaymentBreakUp, Double> paymentBreakUp = getPaymentBreakUp(paymentList);
                mumlyEventPayout.setAmount(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.GROSS_REVENUE));
                mumlyEventPayout.setCommission(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.COMMISSION));
                mumlyEventPayout.setNetAmount(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.NET_REVENUE));
                mumlyEventPayoutRepository.save(mumlyEventPayout);
            } else {
                MumlyEventPayout payout = preparePayout(mumlyEventPaymentRepository.findByEventRegistrationSelectedEventId(event.getId()), event);
                mumlyEventPayoutRepository.save(payout);
            }
        }
    }

    private Map<MumlyEnums.PaymentBreakUp, Double> getPaymentBreakUp(List<MumlyEventPayment> payment) {
        double totalPayment = payment.stream().mapToDouble(MumlyEventPayment::getAmount).sum();
        double commission = 0; //totalPayment * 0.2;
        double netPayment = 0; //totalPayment - commission;
        return Map.of(
                MumlyEnums.PaymentBreakUp.GROSS_REVENUE, totalPayment,
                MumlyEnums.PaymentBreakUp.COMMISSION, commission,
                MumlyEnums.PaymentBreakUp.NET_REVENUE, netPayment);
    }

    private MumlyEventPayout preparePayout(List<MumlyEventPayment> payment, MumlyEvent event) {
        Map<MumlyEnums.PaymentBreakUp, Double> paymentBreakUp = getPaymentBreakUp(payment);
        MumlyEventPayout payout = new MumlyEventPayout();
        payout.setEvent(event);
        payout.setAmount(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.GROSS_REVENUE));
        payout.setCommission(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.COMMISSION));
        payout.setNetAmount(paymentBreakUp.get(MumlyEnums.PaymentBreakUp.NET_REVENUE));
        payout.setPaymentStatus(MumlyEnums.PaymentStatus.PENDING.toString());
        payout.setTransactionId(null);
        payout.setReferenceNo(null);
        payout.setPaymentMode(null);
        payout.setReason(null);
        return payout;
    }

}
