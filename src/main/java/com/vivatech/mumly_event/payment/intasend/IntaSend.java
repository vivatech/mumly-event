package com.vivatech.mumly_event.payment.intasend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivatech.mumly_event.config.ApplicationContextProvider;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.helper.MumlyUtils;
import com.vivatech.mumly_event.payment.PaymentDto;
import com.vivatech.mumly_event.payment.PaymentInterface;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class IntaSend implements PaymentInterface {

    private static final String ISSecretKey;
    private static final String INTA_SEND_URL;
    private static final String INTA_REFUND_URL;

    static {
        Environment env = ApplicationContextProvider.getApplicationContext().getBean(Environment.class);
        ISSecretKey = env.getProperty("iss.secret.key", "");
        INTA_SEND_URL = env.getProperty("inta.send.url", "");
        INTA_REFUND_URL = env.getProperty("inta.refund.url", "");
    }

    @Autowired
    private RestClient restClient;

    @Override
    public boolean supports(MumlyEnums.PaymentMode paymentMode) {
        return paymentMode.equals(MumlyEnums.PaymentMode.INTA_SEND);
    }

    @Override
    public Response sendPayment(PaymentDto paymentDto) {
        IntaSendRequestDto sendRequestDto = IntaSendRequestDto.builder()
                .phone_number(paymentDto.getMsisdn())
                .amount(paymentDto.getAmount().toString())
                .api_ref(paymentDto.getReferenceNo())
                .build();
        ResponseEntity<String> response = restClientCallForSTKPushPayment(sendRequestDto);
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new CustomExceptionHandler("Error calling Mpesa STK Push: " + response.getBody());
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            // Extracting invoice_id from JSON
            String invoiceId = root.path("invoice").path("invoice_id").asText();

            // You can now use invoiceId as needed
            log.info("Invoice ID: {}", invoiceId);
            if (StringUtils.isEmpty(invoiceId)) throw new CustomExceptionHandler("Payment failed.");
            return Response.builder().status("SUCCESS").message("Payment in process.").data(paymentDto.getReferenceNo()).build();
        } catch (Exception e) {
            log.error("Exception while reading json node ", e);
            return Response.builder().status("FAILED").message(e.getMessage()).build();
        }
    }

    private ResponseEntity<String> restClientCallForSTKPushPayment(IntaSendRequestDto requestData) {
        try {
            String response = restClient.post()
                    .uri(INTA_SEND_URL)
                    .header("Authorization", "Bearer " + ISSecretKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(MumlyUtils.makeDtoToJsonString(requestData))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (res, ctx) -> {
                        log.error("Getting 400 error: {}", res.getMethod());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (res, ctx) -> {
                        log.error("Getting 500 error: {}", res.getMethod());
                    })
                    .body(String.class);

            log.info("Response Body of IntaSend: {}", response);
            return ResponseEntity.ok(response);
        } catch (RestClientException ex) {
            log.error("Error calling Mpesa STK Push: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    private ResponseEntity<String> restClientCallForRefundPayment(RefundTicketDto requestData) {
        try {
            String response = restClient.post()
                    .uri(INTA_REFUND_URL)
                    .header("Authorization", "Bearer " + ISSecretKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(MumlyUtils.makeDtoToJsonString(requestData))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (res, ctx) -> {
                        log.error("Getting 400 error - IntaSend Chargeback(Refund): {}", res.getMethod());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (res, ctx) -> {
                        log.error("Getting 500 error - IntaSend Chargeback(Refund): {}", res.getMethod());
                    })
                    .body(String.class);

            log.info("Response Body of IntaSend Chargeback(Refund): {}", response);
            return ResponseEntity.ok(response);
        } catch (RestClientException ex) {
            log.error("Error calling IntaSend Chargeback(Refund): {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    @Override
    public Response reversePayment(PaymentDto dto) {
        RefundTicketDto sendRequestDto = RefundTicketDto.builder()
                .invoice_id(dto.getTransactionId())
                .amount(dto.getAmount().toString())
                .reason(dto.getReason())
                .build();
        ResponseEntity<String> response = restClientCallForRefundPayment(sendRequestDto);
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new CustomExceptionHandler("Error calling IntaSend Chargeback(Refund): " + response.getBody());
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            // Extracting chargeback_id from JSON
            String chargebackId = root.path("chargeback_id").asText();

            // You can now use chargebackId as needed
            log.info("Chargeback ID: {}", chargebackId);
            if (StringUtils.isEmpty(chargebackId)) throw new CustomExceptionHandler("Payment failed.");
            return Response.builder().status("SUCCESS").message("Refund Completed.").data(chargebackId).build();
        } catch (Exception e) {
            log.error("Exception while reading json node ", e);
            return Response.builder().status("FAILED").message(e.getMessage()).build();
        }
    }
}
