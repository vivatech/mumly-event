package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyEvent;
import com.vivatech.mumly_event.model.MumlyEventOrganizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MumlyEventRepository extends JpaRepository<MumlyEvent, Integer> {
    Page<MumlyEvent> findAll(Specification<MumlyEvent> eventSpecification, Pageable pageable);

    List<MumlyEvent> findByCreatedById(Integer id);
    List<MumlyEvent> findTop10ByCreatedByIdOrderByCreatedAtDesc(Integer id);
    List<MumlyEvent> findByEndDateGreaterThanEqual(LocalDate startDate);

    List<MumlyEvent> findByCreatedByAndStartDateGreaterThanEqual(MumlyEventOrganizer organizer, LocalDate oneMonthBefore);
}
