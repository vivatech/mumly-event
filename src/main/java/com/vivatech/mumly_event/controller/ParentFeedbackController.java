package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.PaginationResponse;
import com.vivatech.mumly_event.dto.ParentFeedbackFilter;
import com.vivatech.mumly_event.dto.ParentFeedbackRequest;
import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.model.ParentFeedback;
import com.vivatech.mumly_event.service.ParentFeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event/parent-feedback")
public class ParentFeedbackController {
    
    private final ParentFeedbackService parentFeedbackService;
    
    public ParentFeedbackController(ParentFeedbackService parentFeedbackService) {
        this.parentFeedbackService = parentFeedbackService;
    }
    
    @PostMapping
    public ResponseEntity<ParentFeedback> createFeedback(@RequestBody ParentFeedbackRequest request) {
        ParentFeedback feedback = parentFeedbackService.createFeedback(request);
        return ResponseEntity.ok(feedback);
    }
    
    @PostMapping("/filter")
    public PaginationResponse<ParentFeedback> getAllFeedbacks(@RequestBody ParentFeedbackFilter dto) {
        int size = dto.getSize() != null ? dto.getSize() : EventConstants.PAGE_SIZE;
        Pageable pageable = PageRequest.of(dto.getPageNumber() != null ? dto.getPageNumber() : 0, size);
        Page<ParentFeedback> feedbackFilter = parentFeedbackService.filterEvent(dto, pageable);
        PaginationResponse<ParentFeedback> response = new PaginationResponse<>();
        response.setContent(feedbackFilter.getContent());
        response.setPage(feedbackFilter.getNumber());
        response.setSize(feedbackFilter.getSize());
        response.setTotalElements((int) feedbackFilter.getTotalElements());
        response.setTotalPages(feedbackFilter.getTotalPages());
        return response;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ParentFeedback> getFeedbackById(@PathVariable Integer id) {
        ParentFeedback feedback = parentFeedbackService.getFeedbackById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ParentFeedback> updateFeedback(@PathVariable Integer id, @RequestBody ParentFeedbackRequest request) {
        ParentFeedback updatedFeedback = parentFeedbackService.updateFeedback(id, request);
        return ResponseEntity.ok(updatedFeedback);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer id) {
        parentFeedbackService.deleteFeedback(id);
        return ResponseEntity.noContent().build();
    }
}
