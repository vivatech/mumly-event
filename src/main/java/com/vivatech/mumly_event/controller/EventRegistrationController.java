package com.vivatech.mumly_event.controller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.vivatech.mumly_event.dto.*;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.helper.MumlyUtils;
import com.vivatech.mumly_event.model.MumlyEventPayment;
import com.vivatech.mumly_event.model.Tickets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.vivatech.mumly_event.model.MumlyEvent;

import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.notification.NotificationService;
import com.vivatech.mumly_event.payment.PaymentDto;
import com.vivatech.mumly_event.payment.PaymentService;
import com.vivatech.mumly_event.repository.EventRegistrationRepository;
import com.vivatech.mumly_event.repository.MumlyEventPaymentRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import com.vivatech.mumly_event.service.MumlyEventService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import static com.vivatech.mumly_event.helper.EventConstants.UPLOAD_DIR;
import static com.vivatech.mumly_event.helper.MumlyUtils.formatLocalDateToString;

@Slf4j
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
    @Autowired
    private MumlyEventService mumlyEventService;
    @Autowired
    private NotificationService notificationService;

    @PostMapping
    @Transactional
    public Response createEventRegistration(@RequestBody EventRegistrationRequestDto requestDto) {

        if (validateExistingRegistration(requestDto))
            return Response.builder()
                    .status(MumlyEnums.EventStatus.SUCCESS.toString())
                    .message("Event registration already exists")
                    .build();

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
        notificationService.sendAdminNotification(savedRegistration.getId(), MumlyEnums.NotificationType.REGISTRATION, null);
        requestDto.getPaymentDto().setEventRegistrationId(savedRegistration.getId());
        Response response = paymentService.processPayment(requestDto.getPaymentDto());
        return Response.builder().status(MumlyEnums.EventStatus.SUCCESS.toString()).message("Event registration created successfully").data(response.getData()).build();
    }

    private boolean validateExistingRegistration(EventRegistrationRequestDto requestDto) {
        EventRegistration eventRegistration = eventRegistrationRepository.findByParticipantPhoneAndStatusIn(requestDto.getRegistrationDto().getParticipantPhone(),
                Arrays.asList(MumlyEnums.EventStatus.PENDING.toString(), MumlyEnums.EventStatus.APPROVE.toString()));
        return eventRegistration != null;
    }

    @PutMapping("/{id}")
    public Response updateParticipantStatus(@PathVariable Integer id, @RequestParam MumlyEnums.EventStatus status){
        EventRegistration eventRegistration = eventRegistrationRepository.findById(id).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));
        eventRegistration.setStatus(status.toString());
        eventRegistrationRepository.save(eventRegistration);
        return Response.builder().status(MumlyEnums.EventStatus.SUCCESS.toString()).message("Participant event status " + status + " success.").build();
    }

    @GetMapping("/get-payment-detail")
    public MumlyEventPayment getPaymentDetailByRegistrationId(@RequestParam(value = "registrationId", required = false) Integer participantId,
                                                              @RequestParam(value = "participantPhone", required = false) String participantPhone,
                                                              @RequestParam(value = "eventId", required = false) Integer eventId) {
        EventRegistration eventRegistration = null;
        if (participantId != null) {
            eventRegistration = eventRegistrationRepository.findById(participantId).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));
        } else if (participantPhone != null && eventId != null) {
            eventRegistration = eventRegistrationRepository.findByParticipantPhoneAndSelectedEventId(participantPhone, eventId);
        } else throw new CustomExceptionHandler("Phone and event Id is required");
        MumlyEventPayment payment = mumlyEventPaymentRepository.findByEventRegistration(eventRegistration);
        if (payment == null) throw new CustomExceptionHandler("Payment not found");
        return payment;
    }

    @GetMapping("/ticket-detail")
    public EventTicketDetail getPaymentStatus(@RequestParam String referenceNo) {
        MumlyEventPayment payment = mumlyEventPaymentRepository.findByReferenceNo(referenceNo);
        if (payment == null) throw new CustomExceptionHandler("Payment not found");
        EventTicketDetail ticketDetail = new EventTicketDetail();
        ticketDetail.setEventName(payment.getEventRegistration().getSelectedEvent().getEventName());
        ticketDetail.setEventDate(payment.getEventRegistration().getSelectedEvent().getStartDate());
        ticketDetail.setEventTime(payment.getEventRegistration().getSelectedEvent().getStartTime());
        ticketDetail.setEventLocation(payment.getEventRegistration().getSelectedEvent().getVenueAddress());
        String eventCoverImage = payment.getEventRegistration().getSelectedEvent().getEventCoverImage();
        ticketDetail.setEventImage(!ObjectUtils.isEmpty(eventCoverImage)
                ? EventConstants.EVENT_COVER_PICTURE + payment.getEventRegistration().getSelectedEvent().getId() + "/" + eventCoverImage
                : null);
        ticketDetail.setReferenceNumber(payment.getReferenceNo());
        ticketDetail.setTransactionNo(payment.getTransactionId());
        return ticketDetail;
    }

    @GetMapping("/download-ticket")
    public ResponseEntity<byte[]> downloadTicket(@RequestParam String referenceNo) throws IOException {
        MumlyEventPayment payment = mumlyEventPaymentRepository.findByReferenceNo(referenceNo);
        if (payment == null) {
            throw new RuntimeException("Payment not found");
        }

        EventTicketDetail ticketDetail = new EventTicketDetail();
        ticketDetail.setEventName(payment.getEventRegistration().getSelectedEvent().getEventName());
        ticketDetail.setEventDate(payment.getEventRegistration().getSelectedEvent().getStartDate());
        ticketDetail.setEventTime(payment.getEventRegistration().getSelectedEvent().getStartTime());
        ticketDetail.setEventLocation(payment.getEventRegistration().getSelectedEvent().getVenueAddress());
        ticketDetail.setReferenceNumber(payment.getReferenceNo());
        ticketDetail.setTransactionNo(payment.getTransactionId());
        String eventCoverImage = payment.getEventRegistration().getSelectedEvent().getEventCoverImage();
        ticketDetail.setEventImage(!ObjectUtils.isEmpty(eventCoverImage)
                ? EventConstants.EVENT_COVER_PICTURE + payment.getEventRegistration().getSelectedEvent().getId() + "/" + eventCoverImage
                : null);

        // Create PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A5);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;

            // Display Event Image (if present)
            if (ticketDetail.getEventImage() != null) {
                try {
                    File imageFile = new File(UPLOAD_DIR + "/" + ticketDetail.getEventImage());
                    if (imageFile.exists()) {
                        BufferedImage bufferedImage = ImageIO.read(imageFile);

                        PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

                        float imageWidth = 400;
                        float imageHeight = (bufferedImage.getHeight() * imageWidth) / bufferedImage.getWidth();
                        float startX = (page.getMediaBox().getWidth() - imageWidth) / 2;
                        float startY = yPosition - imageHeight;

                        content.drawImage(pdImage, startX, startY, imageWidth, imageHeight);
                        yPosition = startY - 30;
                    } else {
                        log.warn("Image file not found at: " + imageFile.getAbsolutePath());
                        yPosition -= 30;
                    }
                } catch (Exception e) {
                    log.error("Exception in reading the image: ", e);
                    yPosition -= 30;
                }
            }

            float labelX = 70;
            float valueX = 170;
            float leading = 20; // line spacing

            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.beginText();
            content.newLineAtOffset(70, yPosition);
            content.showText(ticketDetail.getEventName());
            content.endText();

            yPosition -= 40;

            content.setFont(PDType1Font.HELVETICA, 12);

// Draw label-value rows
            drawLabelValue(content, "Date", formatLocalDateToString(ticketDetail.getEventDate()), labelX, valueX, yPosition);
            yPosition -= leading;

            drawLabelValue(content, "Time", ticketDetail.getEventTime(), labelX, valueX, yPosition);
            yPosition -= leading;

            drawLabelValue(content, "Location", ticketDetail.getEventLocation(), labelX, valueX, yPosition);
            yPosition -= leading;

            drawLabelValue(content, "Reference No.", ticketDetail.getReferenceNumber(), labelX, valueX, yPosition);
            yPosition -= leading;

            drawLabelValue(content, "Transaction ID", ticketDetail.getTransactionNo(), labelX, valueX, yPosition);
            yPosition -= (leading + 10);

// Thank you note
            content.setFont(PDType1Font.HELVETICA_BOLD, 13);
            content.beginText();
            content.newLineAtOffset(labelX, yPosition);
            content.showText("Thank you for your purchase!");
            content.endText();

            content.close();
            document.save(baos);
        }

        byte[] pdfBytes = baos.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ticket.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private void drawLabelValue(PDPageContentStream content, String label, String value,
                                float labelX, float valueX, float yPosition) throws IOException {
        content.setFont(PDType1Font.HELVETICA_BOLD, 12);
        content.beginText();
        content.newLineAtOffset(labelX, yPosition);
        content.showText(label);
        content.endText();

        content.setFont(PDType1Font.HELVETICA, 12);
        content.beginText();
        content.newLineAtOffset(valueX, yPosition);
        content.showText(":  " + value);
        content.endText();
    }


    @GetMapping("/receive-cash-payment")
    public Response receiveCashPayment(@RequestParam String referenceNo) {
        MumlyEventPayment payment = mumlyEventPaymentRepository.findByReferenceNo(referenceNo);
        if (payment == null) throw new CustomExceptionHandler("Payment not found");
        String successTransactionId = MumlyUtils.generateRandomString();
        paymentService.processPaymentCallBack(payment.getReferenceNo(), successTransactionId, MumlyEnums.PaymentStatus.COMPLETE.toString(), null);
        return Response.builder().status(MumlyEnums.PaymentStatus.SUCCESS.toString()).message("Payment received successfully").build();
    }

    @PostMapping("/filter")
    public PaginationResponse<EventRegistration> filterEventParticipant(@RequestBody EventRegistrationFilter dto) {
        int size = dto.getSize() != null ? dto.getSize() : EventConstants.PAGE_SIZE;
        Pageable pageable = PageRequest.of(dto.getPageNumber() != null ? dto.getPageNumber() : 0, size);
        Page<EventRegistration> eventPage = eventRegistrationRepository.findAll(mumlyEventService.getEventParticipantsSpecification(dto), pageable);
        PaginationResponse<EventRegistration> response = new PaginationResponse<>();
        response.setContent(updateImagePathInEvents(eventPage.getContent()));
        response.setPage(eventPage.getNumber());
        response.setSize(eventPage.getSize());
        response.setTotalElements((int) eventPage.getTotalElements());
        response.setTotalPages(eventPage.getTotalPages());
        return response;
    }

    @PostMapping("/refund")
    public Response refundTicket(@RequestBody PaymentDto dto) {
        return paymentService.refundTicket(dto);
    }

    private List<EventRegistration> updateImagePathInEvents(List<EventRegistration> registrationList) {
        List<EventRegistration> eventRegistrationList = new ArrayList<>();
        registrationList.forEach(eventRegistration -> {
            MumlyEvent selectedEvent = eventRegistration.getSelectedEvent();
            MumlyEvent eventWithImagePath = mumlyEventService.setImagePath(selectedEvent);
            eventRegistration.setSelectedEvent(eventWithImagePath);
            eventRegistrationList.add(eventRegistration);
        });
        return eventRegistrationList;
    }
}
