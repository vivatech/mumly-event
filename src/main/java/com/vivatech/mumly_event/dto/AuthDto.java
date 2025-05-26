package com.vivatech.mumly_event.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthDto {
    private Integer id;
    private String username;
    private String password;
    private String roles;
    private String status;
    private String message;
    private Date loginTime;
    private Date logoutTime;
    private String token;
    private String otp;
    private String email;

    public AuthDto (String status, String message) {
        this.status = status;
        this.message = message;
    }
}
