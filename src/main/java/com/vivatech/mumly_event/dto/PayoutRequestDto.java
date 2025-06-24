package com.vivatech.mumly_event.dto;

import com.vivatech.mumly_event.helper.MumlyEnums;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayoutRequestDto {
    @NotNull(message = "Event ID is required")
    @Schema(example = "1")
    private Integer eventId;
    @NotNull(message = "Amount is required")
    @Schema(example = "100.00", format = "double", minimum = "1", description = "Total received amount for event")
    private Double amount;
    @NotNull(message = "Commission is required")
    private Double commission;
    @NotNull(message = "Net amount is required")
    private Double netAmount;
    @NotNull(message = "Payment mode is required")
    private MumlyEnums.PaymentMode paymentMode;
    @NotNull(message = "Payment status is required")
    private MumlyEnums.PaymentStatus paymentStatus;
    @NotNull(message = "Reference number is required")
    private String referenceNo;
    @NotNull(message = "Transaction ID is required")
    private String transactionId;
    @NotNull(message = "Transaction date is required")
    private String transactionDate;
    private String reason; // reason if payment failed
}
