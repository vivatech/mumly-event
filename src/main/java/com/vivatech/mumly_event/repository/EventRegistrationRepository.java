package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventRegistration;
import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.Tickets;
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

    List<EventRegistration> findBySelectedEventIdIn(List<Integer> eventIds);

    Integer countByTicketsIn(List<Tickets> tickets);

}
