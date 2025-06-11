package com.vivatech.mumly_event.payment.intasend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefundTicketDto {
    @JsonProperty("invoice_id")
    private String invoice_id; // can be considered as transaction id
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("reason")
    private String reason;
}
