package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.AuthDto;
import com.vivatech.mumly_event.model.MumlyAdmin;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event/admins")
@Slf4j
public class MumlyAdminsController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private MumlyAdminsRepository mumlyAdminsRepository;

    @GetMapping
    public List<MumlyAdmin> getMumlyAdmins() {
        List<MumlyAdmin> mumlyAdmins = mumlyAdminsRepository.findAll();
        log.info("Mumly admins: {}", mumlyAdmins);
        return mumlyAdmins;
    }
}
