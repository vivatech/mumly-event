package com.vivatech.mumly_event.payment.intasend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntaSendPaymentResponse {
    private String invoice_id;
    private String state;
    private String provider;
    private String charges;
    private String net_amount;
    private String currency;
    private String value;
    private String account;
    private String api_ref;
    private String host;
    private String failed_reason;
    private String failed_code;
    private String failed_code_link;
    private Date created_at;
    private Date updated_at;
    private String challenge;
}
