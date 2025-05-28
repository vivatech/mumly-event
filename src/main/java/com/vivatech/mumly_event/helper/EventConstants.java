package com.vivatech.mumly_event.helper;

import com.vivatech.mumly_event.config.ApplicationContextProvider;
import org.springframework.core.env.Environment;

public class EventConstants {

    public static final String EVENT_PROFILE_PICTURE = "EventPicture/";
    public static final String EVENT_COVER_PICTURE = "EventCover/";
    public static final String EVENT_BROCHURE = "EventBrochure/";
    public static final Integer PAGE_SIZE = 30;
    public static final Integer OTP_EXPIRY_TIME = 5;
    public static final String DEFAULT_COUNTRY = "KE";
    public static final Boolean SMS_TESTING;


    static {
        Environment env = ApplicationContextProvider.getApplicationContext().getBean(Environment.class);
        SMS_TESTING = env.getProperty("sms.testing", "false").equals("true");
    }



}
