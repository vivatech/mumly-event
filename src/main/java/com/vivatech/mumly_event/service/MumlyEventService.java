package com.vivatech.mumly_event.service;

import com.vivatech.mumly_event.dto.*;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.repository.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MumlyEventService {

    private final EventCategoryRepository eventCategoryRepository;
    private final FileStorageService fileStorageService;
    private final MumlyEventRepository mumlyEventRepository;
    private final TicketsRepository ticketsRepository;
    private final MumlyAdminsRepository mumlyAdminsRepository;
    private final MumlyEventOrganizerRepository mumlyEventOrganizerRepository;

    public MumlyEventService(EventCategoryRepository eventCategoryRepository, FileStorageService fileStorageService,
                             MumlyEventRepository mumlyEventRepository,
                             TicketsRepository ticketsRepository,
                             MumlyAdminsRepository mumlyAdminsRepository,
                             MumlyEventOrganizerRepository mumlyEventOrganizerRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
        this.fileStorageService = fileStorageService;
        this.mumlyEventRepository = mumlyEventRepository;
        this.ticketsRepository = ticketsRepository;
        this.mumlyAdminsRepository = mumlyAdminsRepository;
        this.mumlyEventOrganizerRepository = mumlyEventOrganizerRepository;
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
            List<String> picturePaths = eventRequestDto.getEventPictureList().stream()
                    .map(ele -> UUID.randomUUID() + "." + fileStorageService.getFileExtension(Objects.requireNonNull(ele.getOriginalFilename())))
                    .toList();
            mumlyEvent.setEventPictureList(picturePaths);
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
        if (mumlyEvent.getId() == null && eventRequestDto.getEventPictureList() != null) {
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
        List<Tickets> tickets = new ArrayList<>();
        if (!ObjectUtils.isEmpty(dto.getTickets())) {
            for (String ticket : dto.getTickets().split(",")) {
                Tickets existingTicket = ticketsRepository.findByTicketType(ticket.trim());
                if (existingTicket != null) tickets.add(existingTicket);
            }
        }
        event.setTickets(tickets);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(exitingUser.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        if (event.getId() == null) {
            event.setCreatedAt(LocalDate.now());
            event.setCreatedBy(organizer);
        } else {
            event.setUpdatedAt(LocalDate.now());
            event.setUpdatedBy(organizer);
        }
        return event;
    }

    private MumlyEvent setImagePath(MumlyEvent mumlyEvent) {
        String eventPicture = mumlyEvent.getEventPicture();
        String eventBrochure = mumlyEvent.getEventBrochure();
        String eventCoverImage = mumlyEvent.getEventCoverImage();
        mumlyEvent.setEventPicture(!ObjectUtils.isEmpty(eventPicture) ? EventConstants.EVENT_PROFILE_PICTURE + mumlyEvent.getId() + "/" + eventPicture : null);
        mumlyEvent.setEventBrochure(!ObjectUtils.isEmpty(eventBrochure) ? EventConstants.EVENT_BROCHURE + mumlyEvent.getId() + "/" + eventBrochure : null);
        mumlyEvent.setEventCoverImage(!ObjectUtils.isEmpty(eventCoverImage) ? EventConstants.EVENT_COVER_PICTURE + mumlyEvent.getId() + "/" + eventCoverImage : null);
        List<String> eventPictureListString = mumlyEvent.getEventPictureList();
        List<String> eventPictureList = new ArrayList<>();
        for (String imageName : eventPictureListString) {
            String filePath = EventConstants.EVENT_PROFILE_PICTURE + mumlyEvent.getId() + "/" + imageName;
            eventPictureList.add(filePath);
        }
        mumlyEvent.setEventPictureList(eventPictureList);
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
        dto.setEventTime(mumlyEvent.getStartTime());
        dto.setEventType(mumlyEvent.getEventType());
        dto.setVenueName(mumlyEvent.getVenueName());
        dto.setVenueAddress(mumlyEvent.getVenueAddress());
        dto.setTicketPrice(mumlyEvent.getTickets());
        dto.setTotalTickets(mumlyEvent.getMaximumNumberOfAttendees());
        dto.setSoldTickets(soldTickets);
        dto.setAvailableTickets(mumlyEvent.getMaximumNumberOfAttendees() - dto.getSoldTickets());
        dto.setEventCoverImage(mumlyEvent.getEventCoverImage());
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

            if (dto.getUsername() != null) {
                MumlyAdmin exitingUser = mumlyAdminsRepository.findByUsername(dto.getUsername());
                MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(exitingUser.getId()).orElse(null);
                if (organizer != null) {
                    Join<MumlyEvent, MumlyEventOrganizer> mumlyEventOrganizerJoin = root.join("createdBy");
                    predicates.add(criteriaBuilder.equal(mumlyEventOrganizerJoin.get("id"), organizer.getId()));
                }
            }

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
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
