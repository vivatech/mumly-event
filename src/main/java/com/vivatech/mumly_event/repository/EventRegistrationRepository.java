package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.Tickets;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {
    Page<EventRegistration> findAll(Specification<EventRegistration> eventSpecification, Pageable pageable);

    List<EventRegistration> findBySelectedEvent(MumlyEvent event);

    Integer countBySelectedEventIn(List<MumlyEvent> eventList);

    Integer countBySelectedEventAndStatusNotIn(MumlyEvent event, List<String> list);

    List<EventRegistration> findBySelectedEventIdIn(List<Integer> eventIds);

    Integer countByTicketsIn(List<Tickets> tickets);

    EventRegistration findByParticipantPhoneAndSelectedEventId(String phone, Integer eventId);

    EventRegistration findByParticipantPhoneAndStatusInAndSelectedEventId(@NotNull(message = "Participant phone number is required") String participantPhone, List<String> list, Integer eventId);

}
