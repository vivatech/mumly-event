package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentFeedbackFilter extends ParentFeedbackRequest {

    private Integer pageNumber;
    private Integer size;
    private LocalDate startDate;
    private LocalDate endDate;
    private String eventOrganizerUserName;
}
