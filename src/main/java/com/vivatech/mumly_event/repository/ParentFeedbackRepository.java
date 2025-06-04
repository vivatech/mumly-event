package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.ParentFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentFeedbackRepository extends JpaRepository<ParentFeedback, Integer> {

    Page<ParentFeedback> findAll(Specification<ParentFeedback> specification, Pageable pageable);

    @Query("SELECT AVG(f.rating) FROM ParentFeedback f WHERE f.event.id IN (:eventId)")
    Double findAverageRatingByEventId(@Param("eventId") List<Integer> eventId);

}
