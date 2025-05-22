package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.model.Tickets;
import com.vivatech.mumly_event.repository.TicketsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event/ticket")
public class TicketController {

    @Autowired
    private TicketsRepository ticketsRepository;

    @PostMapping
    public void createTicket(@RequestBody String ticketType) {
        Tickets tickets = new Tickets();
        tickets.setTicketType(ticketType);
        ticketsRepository.save(tickets);
    }
}
