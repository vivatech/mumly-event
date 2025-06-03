package com.vivatech.mumly_event.payment.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivatech.mumly_event.helper.MumlyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/mpesaCallback")
public class MPaisaCallbackController {

    @Autowired
    private MpaisaCallBackService mPaisaCallbackService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handleSTKPushCallback(@RequestBody MExpressCallbackRequest callbackRequest) {
        // Log the received callback
        String callbackData = MumlyUtils.makeDtoToJsonString(callbackRequest);
        log.info("Received STK Callback: {}", callbackData);

        // Process the callback
        mPaisaCallbackService.processSTKCallback(callbackRequest);

        // Return success response
        return ResponseEntity.ok("Callback received successfully");
    }
}
