package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.AuthDto;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.model.MumlyAdmin;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event/admins")
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

    @PostMapping("/reset-password")
    public AuthDto resetPassword(@RequestBody AuthDto authDto) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByEmail(authDto.getEmail());
        if (mumlyAdmin == null) throw new CustomExceptionHandler("Email not found");
        mumlyAdmin.setPassword(passwordEncoder.encode(authDto.getPassword()));
        mumlyAdminsRepository.save(mumlyAdmin);
        return new AuthDto("SUCCESS", "Password reset success.");
    }
}
