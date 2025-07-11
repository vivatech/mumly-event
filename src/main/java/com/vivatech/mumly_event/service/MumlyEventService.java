package com.vivatech.mumly_event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivatech.mumly_event.dto.*;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.notification.NotificationService;
import com.vivatech.mumly_event.payment.PaymentDto;
import com.vivatech.mumly_event.payment.PaymentGatewayProcessor;
import com.vivatech.mumly_event.payment.PaymentService;
import com.vivatech.mumly_event.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@Service
public class MumlyEventService {

    private final EventCategoryRepository eventCategoryRepository;
    private final FileStorageService fileStorageService;
    private final MumlyEventRepository mumlyEventRepository;
    private final TicketsRepository ticketsRepository;
    private final MumlyAdminsRepository mumlyAdminsRepository;
    private final MumlyEventOrganizerRepository mumlyEventOrganizerRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final MumlyEventPaymentRepository mumlyEventPaymentRepository;
    private final PaymentGatewayProcessor paymentGateway;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final MumlyEventPayoutRepository mumlyEventPayoutRepository;

    public MumlyEventService(EventCategoryRepository eventCategoryRepository, FileStorageService fileStorageService,
                             MumlyEventRepository mumlyEventRepository,
                             TicketsRepository ticketsRepository,
                             MumlyAdminsRepository mumlyAdminsRepository,
                             MumlyEventOrganizerRepository mumlyEventOrganizerRepository,
                             EventRegistrationRepository eventRegistrationRepository,
                             MumlyEventPaymentRepository mumlyEventPaymentRepository,
                             PaymentGatewayProcessor paymentGateway, NotificationService notificationService, PaymentService paymentService,
                             MumlyEventPayoutRepository mumlyEventPayoutRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
        this.fileStorageService = fileStorageService;
        this.mumlyEventRepository = mumlyEventRepository;
        this.ticketsRepository = ticketsRepository;
        this.mumlyAdminsRepository = mumlyAdminsRepository;
        this.mumlyEventOrganizerRepository = mumlyEventOrganizerRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.mumlyEventPaymentRepository = mumlyEventPaymentRepository;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.mumlyEventPayoutRepository = mumlyEventPayoutRepository;
    }

    @Transactional
    public void saveEvent(MumlyEventRequestDto eventRequestDto,
                          MultipartFile eventCoverImageFile,
                          MultipartFile eventPicturesUploadFiles,
                          MultipartFile eventBrochureFile) throws IOException {

        MumlyEvent mumlyEvent = toEntity(eventRequestDto);

        // Save uploaded files
        if (eventCoverImageFile != null && mumlyEvent.getEventCoverImage() == null) {
            String extension = fileStorageService.getFileExtension(Objects.requireNonNull(eventCoverImageFile.getOriginalFilename()));
            String fileName = UUID.randomUUID() + "." + extension;
            mumlyEvent.setEventCoverImage(fileName);
        }
        if (eventBrochureFile != null && mumlyEvent.getEventBrochure() == null) {
            String extension = fileStorageService.getFileExtension(Objects.requireNonNull(eventBrochureFile.getOriginalFilename()));
            String fileName = UUID.randomUUID() + "." + extension;
            mumlyEvent.setEventBrochure(fileName);
        }
        if (eventRequestDto.getEventPictureList() != null) {
            if (mumlyEvent.getId() != null && mumlyEvent.getEventPictureList() != null && !mumlyEvent.getEventPictureList().isEmpty()) {
                mumlyEvent.getEventPictureList()
                        .forEach(ele -> fileStorageService
                                .deleteFile(EventConstants.EVENT_PROFILE_PICTURE + mumlyEvent.getId(), ele));
                mumlyEvent.setEventPictureList(new ArrayList<>());
                List<String> picturePaths = eventRequestDto.getEventPictureList().stream()
                        .map(ele -> UUID.randomUUID() + "." + fileStorageService.getFileExtension(Objects.requireNonNull(ele.getOriginalFilename())))
                        .toList();
                mumlyEvent.setEventPictureList(picturePaths);
            } else if (mumlyEvent.getId() != null && mumlyEvent.getEventPictureList() == null) {
                List<String> picturePaths = eventRequestDto.getEventPictureList().stream()
                        .map(ele -> UUID.randomUUID() + "." + fileStorageService.getFileExtension(Objects.requireNonNull(ele.getOriginalFilename())))
                        .toList();
                mumlyEvent.setEventPictureList(picturePaths);
            } else {
                List<String> picturePaths = eventRequestDto.getEventPictureList().stream()
                        .map(ele -> UUID.randomUUID() + "." + fileStorageService.getFileExtension(Objects.requireNonNull(ele.getOriginalFilename())))
                        .toList();
                mumlyEvent.setEventPictureList(picturePaths);
            }
        }
        if (eventPicturesUploadFiles != null && mumlyEvent.getEventPicture() == null) {
            String fileName = UUID.randomUUID() + "." +  fileStorageService.getFileExtension(Objects.requireNonNull(eventPicturesUploadFiles.getOriginalFilename()));
            mumlyEvent.setEventPicture(fileName);
        }
        MumlyEvent event = mumlyEventRepository.save(mumlyEvent);

        if (eventCoverImageFile != null) {
            String filePath = EventConstants.EVENT_COVER_PICTURE + event.getId();
            fileStorageService.storeFile(eventCoverImageFile, event.getEventCoverImage(), filePath);
        }
        if (eventBrochureFile != null) {
            String filePath = EventConstants.EVENT_BROCHURE + event.getId();
            fileStorageService.storeFile(eventBrochureFile, event.getEventBrochure(), filePath);
        }
        if (eventPicturesUploadFiles != null) {
            String filePath = EventConstants.EVENT_PROFILE_PICTURE + event.getId();
            fileStorageService.storeFile(eventBrochureFile, event.getEventPicture(), filePath);
        }
        //TODO: I will think for the update logic later
        if (eventRequestDto.getEventPictureList() != null) {
            for (String eventPicture : event.getEventPictureList()) {
                String filePath = EventConstants.EVENT_PROFILE_PICTURE + event.getId();
                fileStorageService.storeFile(eventBrochureFile, eventPicture, filePath);
            }
        }
    }



    public MumlyEvent toEntity(MumlyEventRequestDto dto) {

        EventCategory eventCategory = eventCategoryRepository.findById(dto.getEventCategoryId()).orElseThrow(() -> new CustomExceptionHandler("Event category not found"));

        MumlyAdmin exitingUser = mumlyAdminsRepository.findByUsername(dto.getCreatedBy());

        if (exitingUser == null) throw new CustomExceptionHandler("Created by user not found");

        MumlyEvent event = null;
        if (dto.getId() != null) {
            event = mumlyEventRepository.findById(dto.getId()).orElse(new MumlyEvent());
        } else event = new MumlyEvent();
        event.setEventName(dto.getEventName());
        event.setEventCategory(eventCategory);
        event.setEventDescription(dto.getEventDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setTimeZone(dto.getTimeZone());
        event.setEventType(dto.getEventType());
        event.setVenueName(dto.getVenueName());
        event.setVenueAddress(dto.getVenueAddress());
        event.setOrganizerName(dto.getOrganizerName());
        event.setOrganizerContactEmail(dto.getOrganizerContactEmail());
        event.setOrganizerPhoneNumber(dto.getOrganizerPhoneNumber());
        event.setMaximumNumberOfAttendees(dto.getMaximumNumberOfAttendees());
        event.setSpecialInstructions(dto.getSpecialInstructions());
        String ticketsString = dto.getTickets();
        try {
            List<Tickets> dtoTickets = new ObjectMapper().readValue(ticketsString, new TypeReference<List<Tickets>>() {});
            if (event.getTickets() != null && !event.getTickets().isEmpty()){
                for (Tickets dtoTicket : dtoTickets) {
                    boolean ticketTypeChange = event.getTickets().stream().anyMatch(ele -> !ele.getTicketType().equalsIgnoreCase(dtoTicket.getTicketType()));
                    boolean ticketPriceChange = event.getTickets().stream().anyMatch(ele -> ele.getTicketPrice() != dtoTicket.getTicketPrice());
                    if (ticketTypeChange || ticketPriceChange) {
                        Integer registrationList = eventRegistrationRepository.countByTicketsIn(event.getTickets());
                        if (registrationList > 0) throw new CustomExceptionHandler("Ticket type or price cannot be changed as tickets are already registered");
                        List<Integer> ticketIds = event.getTickets().stream().map(Tickets::getId).toList();
                        event.getTickets().clear();
                        ticketsRepository.deleteAllById(ticketIds);
                        List<Tickets> ticketEntities = dtoTickets.stream()
                                .map(t -> {
                                    Tickets ticket = new Tickets();
                                    ticket.setTicketType(t.getTicketType());
                                    ticket.setTicketPrice(t.getTicketPrice());
                                    return ticketsRepository.save(ticket);
                                }).toList();
                        event.getTickets().addAll(ticketEntities);
                    }

                }
            } else {
                List<Tickets> ticketEntities = dtoTickets.stream()
                        .map(t -> {
                            Tickets ticket = new Tickets();
                            ticket.setTicketType(t.getTicketType());
                            ticket.setTicketPrice(t.getTicketPrice());
                            return ticketsRepository.save(ticket);
                        }).toList();
                event.setTickets(ticketEntities);
            }
        } catch (Exception e) {
            throw new CustomExceptionHandler(e.getMessage());
        }
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(exitingUser.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        if (event.getId() == null) {
            event.setCreatedAt(LocalDate.now());
            event.setCreatedBy(organizer);
            event.setEventStatus(MumlyEnums.EventStatus.PENDING.toString());
        } else {
            event.setUpdatedAt(LocalDate.now());
            event.setUpdatedBy(organizer);
        }
        return event;
    }

    public MumlyEvent setImagePath(MumlyEvent mumlyEvent) {
        String eventPicture = mumlyEvent.getEventPicture();
        String eventBrochure = mumlyEvent.getEventBrochure();
        String eventCoverImage = mumlyEvent.getEventCoverImage();
        mumlyEvent.setEventPicture(!ObjectUtils.isEmpty(eventPicture) ? EventConstants.EVENT_PROFILE_PICTURE + mumlyEvent.getId() + "/" + eventPicture : null);
        mumlyEvent.setEventBrochure(!ObjectUtils.isEmpty(eventBrochure) ? EventConstants.EVENT_BROCHURE + mumlyEvent.getId() + "/" + eventBrochure : null);
        mumlyEvent.setEventCoverImage(!ObjectUtils.isEmpty(eventCoverImage) ? EventConstants.EVENT_COVER_PICTURE + mumlyEvent.getId() + "/" + eventCoverImage : null);
        List<String> eventPictureListString = mumlyEvent.getEventPictureList() != null ? mumlyEvent.getEventPictureList() : new ArrayList<>();
        if (!eventPictureListString.isEmpty()) {
            List<String> eventPictureList = new ArrayList<>();
            for (String imageName : eventPictureListString) {
                String filePath = EventConstants.EVENT_PROFILE_PICTURE + mumlyEvent.getId() + "/" + imageName;
                eventPictureList.add(filePath);
            }
            mumlyEvent.setEventPictureList(eventPictureList);
        }
        return mumlyEvent;
    }

    public List<MumlyEventResponseDto> getAllEvent(List<MumlyEvent> allList) {
        List<MumlyEventResponseDto> mumlyEvents = new ArrayList<>();
        for (MumlyEvent mumlyEvent : allList) {
            MumlyEventResponseDto event = convertEntityToDto(setImagePath(mumlyEvent));
            mumlyEvents.add(event);
        }
        return mumlyEvents;
    }

    @Transactional
    public void deleteEvent(Integer eventId) {
        mumlyEventRepository.findById(eventId).ifPresent(mumlyEvent -> {
            String eventPicture = mumlyEvent.getEventPicture();
            String eventBrochure = mumlyEvent.getEventBrochure();
            String eventCoverImage = mumlyEvent.getEventCoverImage();
            List<String> eventPictureList = mumlyEvent.getEventPictureList();
            if (!ObjectUtils.isEmpty(eventPicture)) {
                fileStorageService.deleteFile(EventConstants.EVENT_PROFILE_PICTURE + eventId, eventPicture);
            }
            if (!ObjectUtils.isEmpty(eventBrochure)) {
                fileStorageService.deleteFile(EventConstants.EVENT_BROCHURE + eventId, eventBrochure);
            }
            if (!ObjectUtils.isEmpty(eventCoverImage)) {
                fileStorageService.deleteFile(EventConstants.EVENT_COVER_PICTURE + eventId, eventCoverImage);
            }
            if (!ObjectUtils.isEmpty(eventPictureList)) {
                for (String imageName : eventPictureList) {
                    fileStorageService.deleteFile(EventConstants.EVENT_PROFILE_PICTURE + eventId, imageName);
                }
            }
            mumlyEventRepository.delete(mumlyEvent);
        });
    }

    private MumlyEventResponseDto convertEntityToDto(MumlyEvent mumlyEvent) {
        int soldTickets = 0;
        MumlyEventResponseDto dto = new MumlyEventResponseDto();
        dto.setId(mumlyEvent.getId());
        dto.setEventName(mumlyEvent.getEventName());
        dto.setEventCategory(mumlyEvent.getEventCategory());
        dto.setDescription(mumlyEvent.getEventDescription());
        dto.setEventDate(mumlyEvent.getStartDate());
        dto.setEventEndDate(mumlyEvent.getEndDate());
        dto.setEventTime(mumlyEvent.getStartTime());
        dto.setEventType(mumlyEvent.getEventType());
        dto.setVenueName(mumlyEvent.getVenueName());
        dto.setVenueAddress(mumlyEvent.getVenueAddress());
        dto.setTicketPrice(mumlyEvent.getTickets());
        dto.setTotalTickets(mumlyEvent.getMaximumNumberOfAttendees());
        dto.setSoldTickets(soldTickets);
        dto.setAvailableTickets(mumlyEvent.getMaximumNumberOfAttendees() - dto.getSoldTickets());
        dto.setEventCoverImage(mumlyEvent.getEventCoverImage());
        dto.setEventBrochure(mumlyEvent.getEventBrochure());
        dto.setEventOrganiserName(mumlyEvent.getOrganizerName());
        dto.setEventOrganiserPhone(mumlyEvent.getOrganizerPhoneNumber());
        dto.setEventCreatedBy(mumlyEvent.getCreatedBy().getOrganizerName());
        return dto;
    }

    public Page<MumlyEvent> filterEvent(MumlyEventFilterRequest dto, Pageable pageable) {
        Specification<MumlyEvent> eventSpecification = getEventSpecification(dto);
        return mumlyEventRepository.findAll(eventSpecification, pageable);
    }

    public Specification<MumlyEvent> getEventSpecification(MumlyEventFilterRequest dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            //Filter by document name
            if (!ObjectUtils.isEmpty(dto.getEventCategoryId())) {
                Join<MumlyEvent, EventCategory> mumlyEventJoin = root.join("eventCategory");
                predicates.add(criteriaBuilder.equal(mumlyEventJoin.get("id"), dto.getEventCategoryId()));
            }

            // Filter by Country
            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("startDate"), dto.getStartDate(), dto.getEndDate()));
            }

            if (!ObjectUtils.isEmpty(dto.getUsername())) {
                MumlyAdmin exitingUser = mumlyAdminsRepository.findByUsername(dto.getUsername());
                MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(exitingUser.getId()).orElse(null);
                if (organizer != null) {
                    Join<MumlyEvent, MumlyEventOrganizer> mumlyEventOrganizerJoin = root.join("createdBy");
                    predicates.add(criteriaBuilder.equal(mumlyEventOrganizerJoin.get("id"), organizer.getId()));
                }
            }

            if (!StringUtils.isEmpty(dto.getEventName())) {
                predicates.add(criteriaBuilder.like(root.get("eventName"), "%" + dto.getEventName() + "%"));
            }

            if (!StringUtils.isEmpty(dto.getSearchTerm())) {
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(root.get("eventName"), "%" + dto.getSearchTerm() + "%"),
                        criteriaBuilder.like(root.get("eventCategory").get("name"), "%" + dto.getSearchTerm() + "%"),
                        criteriaBuilder.like(root.get("venueName"), "%" + dto.getSearchTerm() + "%"),
                        criteriaBuilder.like(root.get("venueAddress"), "%" + dto.getSearchTerm() + "%"),
                        criteriaBuilder.like(root.get("eventDescription"), "%" + dto.getSearchTerm() + "%")));
            }

            if (!dto.getDisplayCompletedEvent()) {
                LocalDate today = LocalDate.now();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), today));
            }

            query.orderBy(criteriaBuilder.desc(root.get("createdAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public MumlyEvent getEventById(Integer id) {
        MumlyEvent event = mumlyEventRepository.findById(id).orElseThrow(() -> new CustomExceptionHandler("Event not found"));
        return setImagePath(event);
    }

    public Specification<EventRegistration> getEventParticipantsSpecification(EventRegistrationFilter dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by Date range
            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("startDate"), dto.getStartDate(), dto.getEndDate()));
            }
            if (dto.getUsername() != null) {
                MumlyAdmin exitingUser = mumlyAdminsRepository.findByUsername(dto.getUsername());
                predicates.add(criteriaBuilder.equal(root.get("selectedEvent").get("createdBy").get("adminId"), exitingUser.getId()));
            }
            if (dto.getEventId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("selectedEvent").get("id"), dto.getEventId()));
            }
            if (!StringUtils.isEmpty(dto.getParticipantPhone())) {
                predicates.add(criteriaBuilder.equal(root.get("participantPhone"), dto.getParticipantPhone()));
            }
            if(!StringUtils.isEmpty(dto.getSearchTerm())) {
                Predicate participantNamePredicate = criteriaBuilder.like(root.get("participantName"), "%" + dto.getSearchTerm() + "%");
                Predicate eventNamePredicate = criteriaBuilder.like(root.get("selectedEvent").get("eventName"), "%" + dto.getSearchTerm() + "%");
                predicates.add(criteriaBuilder.or(participantNamePredicate, eventNamePredicate));
            }
            if (!dto.getDisplayCompletedEvent()){
                LocalDate today = LocalDate.now();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("selectedEvent").get("endDate"), today));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Response cancelEvent(Integer eventId, String reason) {
        MumlyEvent event = mumlyEventRepository.findById(eventId).orElseThrow(() -> new CustomExceptionHandler("Event not found"));
        List<EventRegistration> registrationList = eventRegistrationRepository.findBySelectedEvent(event);
        for (EventRegistration registration : registrationList) {
            registration.setStatus(MumlyEnums.EventStatus.CANCELLED.toString());
            registration.setReason(reason);
            eventRegistrationRepository.save(registration);
            MumlyEventPayment payment = mumlyEventPaymentRepository.findByEventRegistration(registration);
            PaymentDto paymentDto = PaymentDto.builder()
                    .transactionId(payment.getTransactionId())
                    .amount(payment.getAmount())
                    .reason(reason)
                    .paymentMode(MumlyEnums.PaymentMode.valueOf(payment.getPaymentMode()))
                    .msisdn(payment.getMsisdn())
                    .eventRegistrationId(payment.getEventRegistration().getId())
                    .build();
            paymentService.refundTicket(paymentDto);
        }
        //TODO: Send the notification to the registered participant
        notificationService.sendAdminNotification(eventId, MumlyEnums.NotificationType.EMERGENCY, reason);

        return Response.builder().status(MumlyEnums.Status.SUCCESS.toString()).message("Event cancelled and payment refund success.").build();
    }

    @Transactional
    public Response savePayoutDetail(PayoutRequestDto dto) {
        MumlyEventPayout mumlyEventPayout = mumlyEventPayoutRepository.findByEventId(dto.getEventId());
        if (mumlyEventPayout == null) throw new CustomExceptionHandler("Payout not found");
        mumlyEventPayout.setCommission(dto.getCommission());
        mumlyEventPayout.setNetAmount(dto.getNetAmount());
        mumlyEventPayout.setPaymentStatus(dto.getPaymentStatus().toString());
        mumlyEventPayout.setTransactionId(dto.getTransactionId());
        mumlyEventPayout.setReferenceNo(dto.getReferenceNo());
        mumlyEventPayout.setPaymentMode(dto.getPaymentMode().toString());
        mumlyEventPayout.setReason(dto.getReason());
        mumlyEventPayoutRepository.save(mumlyEventPayout);
        return Response.builder().status(MumlyEnums.Status.SUCCESS.toString()).message("Payout updated successfully.").build();
    }

    public List<PayoutResponseDto> getPendingPayouts() {

        List<MumlyEventPayout> payouts = mumlyEventPayoutRepository.findByPaymentStatus(MumlyEnums.PaymentStatus.PENDING.toString());
        List<PayoutResponseDto> dtoList = new ArrayList<>();
        for (MumlyEventPayout payout : payouts) {
            PayoutResponseDto dto = new PayoutResponseDto();
            dto.setEventId(payout.getEvent().getId());
            dto.setEventTitle(payout.getEvent().getEventName());
            dto.setEventOrganizerName(payout.getEvent().getOrganizerName());
            dto.setEventCreatedBy(payout.getEvent().getCreatedBy().getOrganizerName());
            dto.setNumberOfParticipants(eventRegistrationRepository
                    .countBySelectedEventAndStatusNotIn(payout.getEvent(),
                            Arrays.asList(MumlyEnums.EventStatus.REFUND.toString(),
                                    MumlyEnums.EventStatus.CANCELLED.toString(),
                                    MumlyEnums.EventStatus.REJECT.toString()
                            )
                    ));
            dto.setAmount(payout.getAmount());
            dto.setCommission(null);
            dto.setNetAmount(null);
            dto.setPaymentStatus(MumlyEnums.PaymentStatus.valueOf(payout.getPaymentStatus()));
            dtoList.add(dto);
        }
        return dtoList;
    }
}
