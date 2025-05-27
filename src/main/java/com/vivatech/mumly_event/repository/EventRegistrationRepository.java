package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Integer> {
}
