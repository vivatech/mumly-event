package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventFeedbackRepository extends JpaRepository<EventFeedback, Integer> {
    List<EventFeedback> findByEventRegistrationId(Integer registrationId);
}
