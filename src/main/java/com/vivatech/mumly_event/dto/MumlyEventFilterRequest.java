package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MumlyEventFilterRequest {
    private Integer eventCategoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer pageNumber;
    private Integer size;
    private String username;
    private String eventName;
    private Boolean displayCompletedEvent = false;
}
