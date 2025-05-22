package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.dto.AuthDto;
import com.vivatech.mumly_event.model.MumlyAdmin;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import com.vivatech.mumly_event.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event/users")
public class LoginController {

    @Autowired
    private MumlyAdminsRepository mumlyAdminsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthDto login(@RequestBody AuthDto authDto) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(authDto.getUsername());
        if (mumlyAdmin != null) {
            if (passwordEncoder.matches(authDto.getPassword(), mumlyAdmin.getPassword())) {
                String token = jwtUtil.generateToken(authDto.getUsername(), 30);
                AuthDto dto = new AuthDto("SUCCESS", "Login success");
                dto.setToken(token);
                return dto;
            } else {
                return new AuthDto("FAILED", "Invalid credentials");
            }
        } else {
            return new AuthDto("FAILED", "Invalid credentials");
        }
    }
}
