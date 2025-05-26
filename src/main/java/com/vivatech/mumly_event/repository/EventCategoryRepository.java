package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.EventCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventCategoryRepository extends JpaRepository<EventCategory, Integer> {

    boolean existsByName(String name);
}
