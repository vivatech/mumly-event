package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDashboardHistory {
    private Integer hostedEvent;
    private Integer draftEvent;
    private Integer publishedEvent;
}
