package com.vivatech.mumly_event.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationFilter extends EventRegistrationDto {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String username;
    private String participantPhone;
    private Integer pageNumber;
    private Integer size;
}
