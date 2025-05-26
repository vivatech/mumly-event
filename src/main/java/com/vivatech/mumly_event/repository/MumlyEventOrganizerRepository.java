package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyEventOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MumlyEventOrganizerRepository extends JpaRepository<MumlyEventOrganizer, Integer> {
    Optional<MumlyEventOrganizer> findByAdminId(Integer id);
}
