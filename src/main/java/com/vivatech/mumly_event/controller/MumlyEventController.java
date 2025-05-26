package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.constenum.EventConstants;
import com.vivatech.mumly_event.dto.MumlyEventFilterRequest;
import com.vivatech.mumly_event.dto.MumlyEventRequestDto;
import com.vivatech.mumly_event.dto.MumlyEventResponseDto;
import com.vivatech.mumly_event.dto.PaginationResponse;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.service.MumlyEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin(allowedHeaders = "*", origins = "*")
@RestController
@RequestMapping("/api/v1/event/events")
public class MumlyEventController {

    @Autowired
    private MumlyEventService service;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@ModelAttribute MumlyEventRequestDto eventRequestDto) throws IOException {
        service.saveEvent(eventRequestDto, eventRequestDto.getEventCoverImageFile(), eventRequestDto.getEventPictureUpload(), eventRequestDto.getEventBrochureFile());
        return ResponseEntity.ok( eventRequestDto.getId() == null ? "Event created successfully!" : "Event updated successfully!");
    }

    @GetMapping("/{id}")
    public MumlyEvent getEventById(@PathVariable Integer id) {
        return service.getEventById(id);
    }

    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer eventId) {
        service.deleteEvent(eventId);
        return ResponseEntity.ok("Event deleted successfully!");
    }

    @PostMapping("/filter")
    public PaginationResponse<MumlyEventResponseDto> filterEvent(@RequestBody MumlyEventFilterRequest dto) {
        int size = dto.getSize() != null ? dto.getSize() : EventConstants.PAGE_SIZE;
        Pageable pageable = PageRequest.of(dto.getPageNumber() != null ? dto.getPageNumber() : 0, size);
        Page<MumlyEvent> eventPage = service.filterEvent(dto, pageable);
        PaginationResponse<MumlyEventResponseDto> response = new PaginationResponse<>();
        response.setContent(service.getAllEvent(eventPage.getContent()));
        response.setPage(eventPage.getNumber());
        response.setSize(eventPage.getSize());
        response.setTotalElements((int) eventPage.getTotalElements());
        response.setTotalPages(eventPage.getTotalPages());
        return response;
    }

}
