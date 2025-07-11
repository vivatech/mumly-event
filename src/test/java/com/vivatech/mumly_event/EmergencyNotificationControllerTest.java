package com.vivatech.mumly_event;

import com.vivatech.mumly_event.controller.NotificationController;
import com.vivatech.mumly_event.dto.AdminNotificationDto;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyNotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private AdminNotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        notificationDto = new AdminNotificationDto();
    }

    @Test
    void testSendEmergencyNotificationWithEventId() throws Exception {
        // Given
        Integer eventId = 1;
        String message = "Test emergency message";
        notificationDto.setEventId(eventId);
        notificationDto.setMessage(message);

        // When
        Response response = notificationController.sendEmergencyNotification(notificationDto);

        // Then
        //verify(notificationService).sendAdminNotification(eq(eventId), eq(MumlyEnums.NotificationType.EMERGENCY), eq(message));
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Notification created successfully. Sending in the background.", response.getMessage());
    }

    @Test
    void testSendEmergencyNotificationWithParticipantIds() throws Exception {
        // Given
        String message = "Test emergency message";
        List<Integer> participantIds = Arrays.asList(1, 2, 3);
        notificationDto.setMessage(message);
        notificationDto.setParticipantIds(participantIds);

        // When
        Response response = notificationController.sendEmergencyNotification(notificationDto);

        // Then
        verify(notificationService).sendEmergencyFeedbackToSelectedParent(eq(participantIds), eq(message));
        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Notification created successfully. Sending in the background.", response.getMessage());
    }

    @Test
    void testSendEmergencyNotificationWithBothEventIdAndParticipantIds() {
        // Given
        Integer eventId = 1;
        List<Integer> participantIds = Arrays.asList(1, 2, 3);
        String message = "Test emergency message";
        notificationDto.setEventId(eventId);
        notificationDto.setParticipantIds(participantIds);
        notificationDto.setMessage(message);

        // When & Then
        assertThrows(Exception.class, () -> {
            notificationController.sendEmergencyNotification(notificationDto);
        });
    }

    @Test
    void testSendEmergencyNotificationWithNullMessage() {
        // Given
        Integer eventId = 1;
        notificationDto.setEventId(eventId);
        notificationDto.setMessage(null);

        // When & Then
        assertThrows(Exception.class, () -> {
            notificationController.sendEmergencyNotification(notificationDto);
        });
    }

    @Test
    void testSendEmergencyNotificationWithEmptyParticipantIds() throws Exception {
        // Given
        String message = "Test emergency message";
        notificationDto.setMessage(message);
        notificationDto.setParticipantIds(Arrays.asList());

        // When
        Response response = notificationController.sendEmergencyNotification(notificationDto);

        // Then
        verify(notificationService, never()).sendEmergencyFeedbackToSelectedParent(any(), any());
    }
}
