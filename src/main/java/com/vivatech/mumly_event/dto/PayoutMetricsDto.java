package com.vivatech.mumly_event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayoutMetricsDto {
    private Integer totalPayout;
    private Integer pendingPayout;
    private Double commission;
    private LocalDate nextPayoutDate;
}
