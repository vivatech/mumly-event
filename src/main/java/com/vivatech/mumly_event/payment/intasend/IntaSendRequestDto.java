package com.vivatech.mumly_event.payment.intasend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IntaSendRequestDto {
    private String amount;
    @JsonProperty("phone_number")
    private String phone_number;
    @JsonProperty("api_ref")
    private String api_ref;
}
