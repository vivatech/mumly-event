package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.MumlyAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MumlyAdminsRepository extends JpaRepository<MumlyAdmin, Integer> {
    MumlyAdmin findByUsername(String username);

}
