package com.vivatech.mumly_event.service;

import com.vivatech.mumly_event.dto.ParentFeedbackFilter;
import com.vivatech.mumly_event.dto.ParentFeedbackRequest;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import com.vivatech.mumly_event.repository.MumlyEventRepository;
import com.vivatech.mumly_event.repository.ParentFeedbackRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParentFeedbackService {
    
    private final ParentFeedbackRepository parentFeedbackRepository;
    private final MumlyEventRepository mumlyEventRepository;
    private final MumlyAdminsRepository mumlyAdminsRepository;


    @Transactional
    public ParentFeedback createFeedback(ParentFeedbackRequest request) {
        MumlyEvent event = mumlyEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        ParentFeedback feedback = new ParentFeedback();

        feedback.setParentName(request.getParentName());
        feedback.setRating(request.getRating());
        feedback.setEvent(event);
        feedback.setComment(request.getComment());
        if (request.getSubmittedById() != null) feedback.setSubmittedById(request.getSubmittedById());
        else {
            MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(request.getUsername());
            if (mumlyAdmin != null) feedback.setSubmittedById(mumlyAdmin.getId());
        }
        feedback.setFeedbackDate(LocalDate.now());
        
        return parentFeedbackRepository.save(feedback);
    }
    
    public Optional<ParentFeedback> getFeedbackById(Integer id) {
        return parentFeedbackRepository.findById(id);
    }
    
    @Transactional
    public ParentFeedback updateFeedback(Integer id, ParentFeedbackRequest request) {
        ParentFeedback feedback = parentFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        
        MumlyEvent event = mumlyEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Participant not found"));
        
        feedback.setParentName(request.getParentName());
        feedback.setRating(request.getRating());
        feedback.setEvent(event);
        feedback.setComment(request.getComment());
        if (request.getSubmittedById() != null) feedback.setSubmittedById(request.getSubmittedById());
        else {
            MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(request.getUsername());
            if (mumlyAdmin != null) feedback.setSubmittedById(mumlyAdmin.getId());
        }
        feedback.setFeedbackDate(LocalDate.now());
        
        return parentFeedbackRepository.save(feedback);
    }
    
    @Transactional
    public void deleteFeedback(Integer id) {
        ParentFeedback feedback = parentFeedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
        parentFeedbackRepository.delete(feedback);
    }

    public Page<ParentFeedback> filterEvent(ParentFeedbackFilter dto, Pageable pageable) {
        Specification<ParentFeedback> eventSpecification = getParentFeedbackSpecification(dto);
        return parentFeedbackRepository.findAll(eventSpecification, pageable);
    }

    public Specification<ParentFeedback> getParentFeedbackSpecification(ParentFeedbackFilter dto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (!ObjectUtils.isEmpty(dto.getEventId())) {
                Join<ParentFeedback, MumlyEvent> mumlyEventJoin = root.join("event");
                predicates.add(criteriaBuilder.equal(mumlyEventJoin.get("id"), dto.getEventId()));
            }

            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("startDate"), dto.getStartDate(), dto.getEndDate()));
            }

            if (dto.getSubmittedById() != null) {
                predicates.add(criteriaBuilder.equal(root.get("submittedById"), dto.getSubmittedById()));
            }

            if (!ObjectUtils.isEmpty(dto.getUsername())) {
                MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(dto.getUsername());
                if (mumlyAdmin != null) predicates.add(criteriaBuilder.equal(root.get("submittedById"), mumlyAdmin.getId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
