package com.vivatech.mumly_event.repository;

import com.vivatech.mumly_event.model.Tickets;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketsRepository extends JpaRepository<Tickets, Integer> {
    Tickets findByTicketType(String ticket);
}
