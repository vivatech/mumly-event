package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MumlyEventRepository extends JpaRepository<MumlyEvent, Integer> {
}
