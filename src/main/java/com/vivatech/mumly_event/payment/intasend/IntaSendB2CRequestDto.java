package com.vivatech.mumly_event.payment.intasend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class IntaSendB2CRequestDto {
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("provider")
    private String provider;
    
    @JsonProperty("transactions")
    private List<Transaction> transactions;
    
    @Data
    public static class Transaction {
        @JsonProperty("name")
        private String name;
        
        @JsonProperty("account")
        private String account;
        
        @JsonProperty("amount")
        private String amount;
    }
}
