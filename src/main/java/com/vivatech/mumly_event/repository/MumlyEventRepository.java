package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MumlyEventRepository extends JpaRepository<MumlyEvent, Integer> {
    Page<MumlyEvent> findAll(Specification<MumlyEvent> eventSpecification, Pageable pageable);

    List<MumlyEvent> findByCreatedById(Integer id);
}
