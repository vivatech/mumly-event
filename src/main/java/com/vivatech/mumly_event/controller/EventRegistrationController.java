package com.vivatech.mumly_event.controller;
import com.vivatech.mumly_event.dto.EventRegistrationRequestDto;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.helper.MumlyUtils;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import com.vivatech.mumly_event.model.Tickets;
import java.time.LocalDateTime;
import java.util.Objects;

import com.vivatech.mumly_event.model.MumlyEvent;

import com.vivatech.mumly_event.dto.EventRegistrationDto;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.payment.PaymentService;
import com.vivatech.mumly_event.repository.EventRegistrationRepository;
import com.vivatech.mumly_event.repository.MumlyEventPaymentRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event/event-registration")
public class EventRegistrationController {

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;
    @Autowired
    private MumlyEventRepository mumlyEventRepository;
    @Autowired
    private MumlyEventPaymentRepository mumlyEventPaymentRepository;
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    @Transactional
    public Response createEventRegistration(@RequestBody EventRegistrationRequestDto requestDto) {
        EventRegistrationDto dto = requestDto.getRegistrationDto();
        EventRegistration eventRegistration = new EventRegistration();
        eventRegistration.setParticipantName(dto.getParticipantName());
        eventRegistration.setGuardianName(dto.getGuardianName());
        eventRegistration.setParticipantEmail(dto.getParticipantEmail());
        eventRegistration.setParticipantPhone(dto.getParticipantPhone());
        eventRegistration.setParticipantAddress(dto.getParticipantAddress());
        eventRegistration.setParticipantGender(dto.getParticipantGender());
        eventRegistration.setParticipantAge(dto.getParticipantAge());
        MumlyEvent event = mumlyEventRepository.findById(dto.getEventId()).orElseThrow(() -> new CustomExceptionHandler("Event not found"));
        eventRegistration.setSelectedEvent(event);
        Tickets tickets = event.getTickets().stream().filter(ele -> Objects.equals(ele.getId(), dto.getTicketId())).findFirst().orElseThrow(() -> new CustomExceptionHandler("Ticket not found"));
        eventRegistration.setTickets(tickets);
        eventRegistration.setStatus(MumlyEnums.EventStatus.PENDING.toString());
        eventRegistration.setCreatedAt(LocalDateTime.now());
        EventRegistration savedRegistration = eventRegistrationRepository.save(eventRegistration);
        requestDto.getPaymentDto().setEventRegistrationId(savedRegistration.getId());
        paymentService.processPayment(requestDto.getPaymentDto());
        return Response.builder().status(MumlyEnums.EventStatus.SUCCESS.toString()).message("Event registration created successfully").build();
    }

    @GetMapping("/{id}")
    public Response updateParticipantStatus(@PathVariable Integer id, @RequestParam MumlyEnums.EventStatus status){
        EventRegistration eventRegistration = eventRegistrationRepository.findById(id).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));
        eventRegistration.setStatus(status.toString());
        eventRegistrationRepository.save(eventRegistration);
        return Response.builder().status(MumlyEnums.EventStatus.SUCCESS.toString()).message("Participant event status " + status + " success.").build();
    }

    @GetMapping("/receive-cash-payment")
    public Response receiveCashPayment(@RequestParam String referenceNo) {
        MumlyEventPayment payment = mumlyEventPaymentRepository.findByReferenceNo(referenceNo);
        if (payment == null) throw new CustomExceptionHandler("Payment not found");
        String successTransactionId = MumlyUtils.generateRandomString();
        paymentService.processPaymentCallBack(payment.getReferenceNo(), successTransactionId, MumlyEnums.PaymentStatus.SUCCESS.toString());
        return Response.builder().status(MumlyEnums.PaymentStatus.SUCCESS.toString()).message("Payment received successfully").build();
    }
}
