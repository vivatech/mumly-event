package com.vivatech.mumly_event.payment;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyUtils;
import com.vivatech.mumly_event.model.EventRegistration;
import java.time.LocalDateTime;

import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import com.vivatech.mumly_event.notification.NotificationService;
import com.vivatech.mumly_event.payment.intasend.RefundTicketDto;
import com.vivatech.mumly_event.repository.EventRegistrationRepository;
import com.vivatech.mumly_event.repository.MumlyEventPaymentRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentService {
    @Autowired
    private MumlyEventRepository mumlyEventRepository;
    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private PaymentGatewayProcessor paymentGateway;

    @Autowired
    private MumlyEventPaymentRepository paymentRepository;

    @Autowired
    private NotificationService notificationService;

    public Response processPayment(PaymentDto paymentDto) {
        Response response = new Response();
        log.info("Processing payment");
        MumlyEventPayment eventPayment = saveNewPaymentEntity(paymentDto);
        paymentDto.setReferenceNo(eventPayment.getReferenceNo());
        Response paymentResponse = paymentGateway.sendPayment(paymentDto, paymentDto.getPaymentMode());
        if (paymentResponse.getStatus().equalsIgnoreCase(MumlyEnums.PaymentStatus.SUCCESS.toString())) {
            String merchantReferenceNumber = (String) paymentResponse.getData();
            eventPayment.setReferenceNo(merchantReferenceNumber);
            response.setStatus(MumlyEnums.PaymentStatus.SUCCESS.toString());
        } else {
            eventPayment.setPaymentStatus(MumlyEnums.PaymentStatus.FAILED.toString());
            eventPayment.setReason(paymentResponse.getMessage());
            eventPayment.getEventRegistration().setStatus(MumlyEnums.Status.FAILED.toString());
            response.setStatus(MumlyEnums.PaymentStatus.FAILED.toString());
        }
        MumlyEventPayment savedPayment = paymentRepository.save(eventPayment);
        response.setMessage("Reference No: " + savedPayment.getReferenceNo());
        response.setData(savedPayment.getReferenceNo());
        notificationService.sendAdminNotification(savedPayment.getId(), MumlyEnums.NotificationType.PAYMENT, null);
        return response;
    }

    public void processPaymentCallBack(String referenceNo, String transactionId, String paymentStatus, String reason) {
        MumlyEventPayment payment = paymentRepository.findByReferenceNo(referenceNo);
        if (payment == null) throw new CustomExceptionHandler("Payment not found");
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus(paymentStatus);
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setReason(reason);
        paymentRepository.save(payment);
    }

    public MumlyEventPayment saveNewPaymentEntity(PaymentDto paymentDto) {
        MumlyEventPayment payment = new MumlyEventPayment();
        payment.setMsisdn(paymentDto.getMsisdn());
        payment.setAmount(paymentDto.getAmount());
        payment.setReferenceNo(MumlyUtils.generateRandomString());
        payment.setPaymentMode(paymentDto.getPaymentMode().toString());
        payment.setPaymentStatus(MumlyEnums.PaymentStatus.PENDING.toString());
        payment.setCreatedAt(LocalDateTime.now());
        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setId(paymentDto.getEventRegistrationId());
        payment.setEventRegistration(eventRegistration);
        return paymentRepository.save(payment);
    }

    public Response refundTicket(PaymentDto dto) {
        //Transaction ID means the Invoice ID which you got from the Aggregator
        MumlyEventPayment payment = paymentRepository.findByTransactionId(dto.getTransactionId());
        if (payment == null) return Response.builder().status(MumlyEnums.Status.FAILED.toString()).message("Payment not found").build();
        dto.setPaymentMode(MumlyEnums.PaymentMode.valueOf(payment.getPaymentMode()));
        dto.setMsisdn(payment.getMsisdn());
        dto.setEventRegistrationId(payment.getEventRegistration().getId());

        MumlyEventPayment refundPayment = saveNewPaymentEntity(dto);
        refundPayment.setReferenceNo(payment.getTransactionId());

        Response response = paymentGateway.refundPayment(dto, dto.getPaymentMode());
        if (response.getStatus().equalsIgnoreCase(MumlyEnums.PaymentStatus.SUCCESS.toString())) {
            refundPayment.setTransactionId((String) response.getData());
            refundPayment.setPaymentStatus(MumlyEnums.PaymentStatus.REFUND.toString());
            refundPayment.setUpdatedAt(LocalDateTime.now());
            refundPayment.setReason(dto.getReason());
            payment.setAmount(0.0);
            payment.setReason("Amount refunded. The chargeback ID is: " + refundPayment.getTransactionId());
        } else {
            refundPayment.setPaymentStatus(MumlyEnums.PaymentStatus.REFUND_FAILED.toString());
            refundPayment.setReason(response.getMessage());
        }
        MumlyEventPayment savedPayment = paymentRepository.save(refundPayment);
        paymentRepository.save(payment);
        if (savedPayment.getPaymentStatus().equalsIgnoreCase(MumlyEnums.PaymentStatus.REFUND.toString())) {
            EventRegistration eventRegistration = eventRegistrationRepository.findById(savedPayment.getEventRegistration().getId()).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));
            eventRegistration.setStatus(MumlyEnums.EventStatus.REFUND.toString());
            eventRegistration.setReason(savedPayment.getReason());
            EventRegistration registration = eventRegistrationRepository.save(eventRegistration);
            //After the refund success update the number of registered attendees
            registration.getSelectedEvent().setRegisteredAttendees(registration.getSelectedEvent().getRegisteredAttendees() - 1);
            mumlyEventRepository.save(registration.getSelectedEvent());
        }
        return response;
    }
}
