package com.vivatech.mumly_event.payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivatech.mumly_event.config.ApplicationContextProvider;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.helper.MumlyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class SafariPayment implements PaymentInterface {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MpaisaCallBackService callBackService;

    @Autowired
    private RestClient restClient;

    public static final String SAFARI_COM_BASE_URL;
    public static final String SAFARI_COM_CONSUMER_KEY;
    public static final String SAFARI_COM_CONSUMER_SECRET;
    public static final String SAFARI_COM_EXPRESS_BUSINESS_SHORTCODE;
    public static final String SAFARI_COM_EXPRESS_PASSKEY;
    public static final String SAFARI_COM_EXPRESS_CALLBACK_URL;
    public static final String SAFARI_COM_EXPRESS_PARTY_B;
    public static final String SAFARI_COM_EXPRESS_ACCOUNT_REFERENCE;
    public static final String SAFARI_COM_EXPRESS_TRANSACTION_DESC;
    public static final Boolean SAFARI_COM_EXPRESS_TESTING;

    static {
        Environment env = ApplicationContextProvider.getApplicationContext().getBean(Environment.class);

        SAFARI_COM_BASE_URL = env.getProperty("safaricom.api.base-url", "");
        SAFARI_COM_CONSUMER_KEY = env.getProperty("safaricom.api.consumer-key", "");
        SAFARI_COM_CONSUMER_SECRET = env.getProperty("safaricom.api.consumer-secret", "");
        SAFARI_COM_EXPRESS_BUSINESS_SHORTCODE = env.getProperty("safaricom.express.business.shortcode", "");
        SAFARI_COM_EXPRESS_PASSKEY = env.getProperty("safaricom.express.passkey", "");
        SAFARI_COM_EXPRESS_CALLBACK_URL = env.getProperty("safaricom.express.callback.url", "");
        SAFARI_COM_EXPRESS_PARTY_B = env.getProperty("safaricom.express.partyB", "");
        SAFARI_COM_EXPRESS_ACCOUNT_REFERENCE = env.getProperty("safaricom.express.account-ref", "");
        SAFARI_COM_EXPRESS_TRANSACTION_DESC = env.getProperty("safaricom.express.trans-desc", "");
        SAFARI_COM_EXPRESS_TESTING = env.getProperty("payment.testing", "true").equals("true");
    }

    @Override
    public boolean supports(MumlyEnums.PaymentMode paymentMode) {
        return paymentMode.equals(MumlyEnums.PaymentMode.MOBILE_MONEY);
    }

    @Override
    public Response sendPayment(PaymentDto paymentDto) {
        return sendStkPushRequest(paymentDto.getMsisdn(), paymentDto.getAmount());
    }

    @Override
    public Response reversePayment(String msisdn, String transactionId) {
        return new Response();
    }

    public HashMap<String, Object> getAccessToken() {
        // Build the URL
        String url = UriComponentsBuilder
                .fromUriString(SAFARI_COM_BASE_URL + "/oauth/v1/generate")
                .queryParam("grant_type", "client_credentials")
                .toUriString();

        // Create the Authorization header
        String credentials = SAFARI_COM_CONSUMER_KEY + ":" + SAFARI_COM_CONSUMER_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request = new HttpEntity<>(headers);

        // Logging the request
        log.info("Sending Request To URL: {}", url);
        log.info("Get Access Token - Request Headers: {}", headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        // Logging the response
        log.info("Get Access Token - Response Status: {}", response.getStatusCode());
        log.info("Get Access Token - Response Body: {}", response.getBody());

        // Parse the response to a HashMap
        HashMap<String, Object> responseMap = convertToHashMap(response.getBody());

        // Logging the parsed response map
        log.info("Get Access Token - Parsed Response Map: {}", responseMap);

        return responseMap;
    }

    private HashMap<String, Object> convertToHashMap(String json) {
        try {
            return objectMapper.readValue(json, HashMap.class);
        } catch (Exception ex) {
            log.error("Error while converting JSON to HashMap: {}", ex.getMessage(), ex);
            return null;
        }
    }

    public Response sendStkPushRequest(String msisdn, double amount) {
        // Generate Timestamp
        String timestamp = MumlyUtils.generateRandomString();

        // Generate Password
        String password = Base64.getEncoder().encodeToString((SAFARI_COM_EXPRESS_BUSINESS_SHORTCODE + SAFARI_COM_EXPRESS_PASSKEY + timestamp).getBytes());

        // Fetch the access token
        HashMap<String, Object> accessTokenResponse = getAccessToken();
        String accessToken = (String) accessTokenResponse.get("access_token");

        // Build the URL
        /**This code is deprecated
            String url = UriComponentsBuilder.fromHttpUrl(SAFARI_COM_BASE_URL + "/mpesa/stkpush/v1/processrequest").toUriString();
        */
        String url = UriComponentsBuilder.fromUriString(SAFARI_COM_BASE_URL + "/mpesa/stkpush/v1/processrequest").toUriString();

        // Prepare the payload
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("BusinessShortCode", SAFARI_COM_EXPRESS_BUSINESS_SHORTCODE);
        requestBody.put("Password", password);
        requestBody.put("Timestamp", timestamp);
        requestBody.put("TransactionType", "CustomerPayBillOnline");
        requestBody.put("Amount", amount);
        requestBody.put("PartyA", msisdn);
        requestBody.put("PartyB", SAFARI_COM_EXPRESS_PARTY_B);
        requestBody.put("PhoneNumber", msisdn);
        requestBody.put("CallBackURL", SAFARI_COM_EXPRESS_CALLBACK_URL);
        requestBody.put("AccountReference", SAFARI_COM_EXPRESS_ACCOUNT_REFERENCE);
        requestBody.put("TransactionDesc", SAFARI_COM_EXPRESS_TRANSACTION_DESC);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Build the request entity
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Logging the request
        log.info("Sending request to URL: {}", url);
        log.info("Request Headers: {}", headers);
        log.info("Request Body: {}", requestBody);

        // Send the request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = !SAFARI_COM_EXPRESS_TESTING
                ? restClientCall(url, requestBody, accessToken)
                : ResponseEntity.ok(dummyStkPushResponse());



        // Logging the response
        log.info("Mpesa Response Status: {}", response.getStatusCode());
        log.info("Mpesa Response Body: {}", response.getBody());

        // Parse and return the response as HashMap
        HashMap<String, Object> responseMap = convertToHashMap(response.getBody());
        log.info("Parsed Response Map: {}", responseMap);

        if (SAFARI_COM_EXPRESS_TESTING) {
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                callBackService.processSTKCallback(getDummyCallbackRequest());
            });
        }

        if (responseMap != null) {
            if (responseMap.containsKey("errorMessage")) {
                return Response.builder().status("FAILED").message(responseMap.get("errorMessage").toString()).build();
            }
        }

        //Use this as the transaction identifier - response.get("MerchantRequestID").toString()

        return Response.builder().status("SUCCESS").message("Payment in process.").data(responseMap != null ? responseMap.get("MerchantRequestID").toString() : null).build();
    }

    private ResponseEntity<String> restClientCall(String url, Map<String, Object> requestData, String accessToken) {
        try {
            String response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestData)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (res, ctx) -> {
                        log.error("Getting 400 error: {}", res.getMethod());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (res, ctx) -> {
                        log.error("Getting 500 error: {}", res.getMethod());
                    })
                    .body(String.class);

            return ResponseEntity.ok(response);
        } catch (RestClientException ex) {
            log.error("Error calling Mpesa STK Push: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    private String dummyStkPushResponse(){
        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("MerchantRequestID", MumlyUtils.generateRandomString());
        responseMap.put("CheckoutRequestID", "ws_CO_" + MumlyUtils.generateRandomString());
        responseMap.put("ResponseCode", "0");
        responseMap.put("ResponseDescription", "Success. Request accepted for processing");
        responseMap.put("CustomerMessage", "Success. Request accepted for processing");
        return MumlyUtils.makeDtoToJsonString(responseMap);
    }

    private MExpressCallbackRequest getDummyCallbackRequest() {
        MExpressCallbackRequest mExpressCallbackRequest = null;
        try {
            String json = "{\"Body\":{\"stkCallback\":{\"MerchantRequestID\":\"29115-34620561-1\",\"CheckoutRequestID\":\"ws_CO_191220191020363925\",\"ResultCode\":0,\"ResultDesc\":\"The service request is processed successfully.\",\"CallbackMetadata\":{\"Item\":[{\"Name\":\"Amount\",\"Value\":1.00},{\"Name\":\"MpesaReceiptNumber\",\"Value\":\"NLJ7RT61SV\"},{\"Name\":\"TransactionDate\",\"Value\":20191219102115},{\"Name\":\"PhoneNumber\",\"Value\":254708374149}]}}}}";

            mExpressCallbackRequest = new ObjectMapper().readValue(json, MExpressCallbackRequest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mExpressCallbackRequest;
    }


}
