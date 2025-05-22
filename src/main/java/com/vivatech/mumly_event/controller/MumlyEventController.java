package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.MumlyEventRequestDto;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.service.MumlyEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/event")
public class MumlyEventController {

    @Autowired
    private MumlyEventService service;

    @PostMapping("/create")
    public ResponseEntity<String> createEvent(@ModelAttribute MumlyEventRequestDto eventRequestDto) throws IOException {
        service.saveEvent(eventRequestDto, eventRequestDto.getEventCoverImageFile(), eventRequestDto.getEventPictureUpload(), eventRequestDto.getEventBrochureFile());
        return ResponseEntity.ok("Event created successfully!");
    }

    @GetMapping("/all")
    public List<MumlyEvent> getAllEvent() {
        return service.getAllEvent();
    }

    @DeleteMapping("/delete/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Integer eventId) {
        service.deleteEvent(eventId);
        return ResponseEntity.ok("Event deleted successfully!");
    }

}
