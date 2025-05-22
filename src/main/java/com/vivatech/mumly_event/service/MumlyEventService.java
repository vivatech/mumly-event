package com.vivatech.mumly_event.service;

import com.vivatech.mumly_event.constenum.EventConstants;
import com.vivatech.mumly_event.dto.MumlyEventRequestDto;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.model.EventCategory;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.Tickets;
import com.vivatech.mumly_event.repository.EventCategoryRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import com.vivatech.mumly_event.repository.TicketsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public MumlyEventService(EventCategoryRepository eventCategoryRepository, FileStorageService fileStorageService,
                             MumlyEventRepository mumlyEventRepository,
                             TicketsRepository ticketsRepository) {
        this.eventCategoryRepository = eventCategoryRepository;
        this.fileStorageService = fileStorageService;
        this.mumlyEventRepository = mumlyEventRepository;
        this.ticketsRepository = ticketsRepository;
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

        MumlyEvent event = mumlyEventRepository.findById(dto.getId()).orElse(new MumlyEvent());
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
        return event;
    }

    public List<MumlyEvent> getAllEvent() {
        List<MumlyEvent> allList = mumlyEventRepository.findAll();
        List<MumlyEvent> mumlyEvents = new ArrayList<>();
        for (MumlyEvent mumlyEvent : allList) {
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
            mumlyEvents.add(mumlyEvent);
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
}
