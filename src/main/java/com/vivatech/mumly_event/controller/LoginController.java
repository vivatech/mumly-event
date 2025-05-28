package com.vivatech.mumly_event.controller;

import com.vivatech.mumly_event.helper.EventConstants;
import com.vivatech.mumly_event.dto.AuthDto;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.model.MumlyAdmin;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import com.vivatech.mumly_event.security.JwtUtil;
import com.vivatech.mumly_event.helper.MumlyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v1/event/users")
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
                //TODO: set the otp to the mumlyAdmin entity and send the OTP message
                int otp = EventConstants.SMS_TESTING ? 1234 : 1000 + (int)(Math.random() * 9000);
                log.info("Type: AUTHENTICATION, Username : {}, OTP: {}", authDto.getUsername(), otp);
                String formattedOtp = String.format("%04d", otp);
                mumlyAdmin.setOtp(formattedOtp);
                mumlyAdmin.setCreatedAt(LocalDateTime.now());
                mumlyAdminsRepository.save(mumlyAdmin);
                return new AuthDto("SUCCESS", "Login success. OTP sent to the registered mobile number");
            } else {
                return new AuthDto("FAILED", "Invalid credentials");
            }
        } else {
            return new AuthDto("FAILED", "Invalid credentials");
        }
    }

    @PostMapping("/verify-otp")
    public AuthDto verifyOtp(@RequestBody AuthDto authDto) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(authDto.getUsername());
        if (mumlyAdmin == null) throw new CustomExceptionHandler("User not found");
        log.info("Created Date: {}, Expiry Date: {}", mumlyAdmin.getCreatedAt(), MumlyUtils.addMinutesToJavaUtilDate(mumlyAdmin.getCreatedAt(), EventConstants.OTP_EXPIRY_TIME));
        log.info("IsExpired: {}", MumlyUtils.addMinutesToJavaUtilDate(mumlyAdmin.getCreatedAt(), EventConstants.OTP_EXPIRY_TIME).isBefore(LocalDateTime.now()));
        if (mumlyAdmin.getOtp().equals(authDto.getOtp())
                && !MumlyUtils.addMinutesToJavaUtilDate(mumlyAdmin.getCreatedAt(), EventConstants.OTP_EXPIRY_TIME)
                .isBefore(LocalDateTime.now())) {
            String token = jwtUtil.generateToken(authDto.getUsername(), 30);
            AuthDto dto = new AuthDto("SUCCESS", "OTP verified.");
            dto.setToken(token);
            return dto;
        } else {
            return new AuthDto("FAILED", "Invalid OTP");
        }
    }

    @PostMapping("/forgot-password")
    public AuthDto forgotPassword(@RequestBody AuthDto authDto) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByEmail(authDto.getEmail());
        if (mumlyAdmin == null) throw new CustomExceptionHandler("Email not found");
        //TODO: set the otp to the mumlyAdmin entity and send the OTP message
        int otp = 1000 + (int)(Math.random() * 9000);
        log.info("Type: FORGOT_PASSWORD, Email : {}, OTP: {}", authDto.getEmail(), otp);
        String formattedOtp = String.format("%04d", otp);
        mumlyAdmin.setOtp(formattedOtp);
        mumlyAdmin.setCreatedAt(LocalDateTime.now());
        mumlyAdminsRepository.save(mumlyAdmin);
        return new AuthDto("SUCCESS", "OTP sent to the registered email");
    }

    @PostMapping("/verify-forgot-password-otp")
    public AuthDto verifyForgotPasswordOtp(@RequestBody AuthDto authDto) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByEmail(authDto.getEmail());
        if (mumlyAdmin == null) throw new CustomExceptionHandler("Email not found");
        if (mumlyAdmin.getOtp().equals(authDto.getOtp())
                && !MumlyUtils.addMinutesToJavaUtilDate(mumlyAdmin.getCreatedAt(), EventConstants.OTP_EXPIRY_TIME)
                .isBefore(LocalDateTime.now())) {
            String token = jwtUtil.generateToken(authDto.getEmail(), 30);
            AuthDto dto = new AuthDto("SUCCESS", "OTP verified.");
            dto.setToken(token);
            return dto;
        } else return new AuthDto("FAILED", "Invalid OTP");

    }
}
