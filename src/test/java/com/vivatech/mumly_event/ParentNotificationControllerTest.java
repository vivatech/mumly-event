package com.vivatech.mumly_event;

import com.vivatech.mumly_event.controller.NotificationController;
import com.vivatech.mumly_event.notification.model.AdminNotification;
import com.vivatech.mumly_event.notification.repository.AdminNotificationRepository;
import com.vivatech.mumly_event.repository.MumlyAdminsRepository;
import com.vivatech.mumly_event.repository.MumlyEventOrganizerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParentNotificationControllerTest {

    @Mock
    private AdminNotificationRepository adminNotificationRepository;

    @Mock
    private MumlyEventOrganizerRepository mumlyEventOrganizerRepository;

    @Mock
    private MumlyAdminsRepository mumlyAdminsRepository;

    @InjectMocks
    private NotificationController notificationController;

    private AdminNotification mockNotification;

    @BeforeEach
    void setUp() {
        mockNotification = new AdminNotification();
        mockNotification.setId(1);
        mockNotification.setMessage("Test notification");
        mockNotification.setReceiverMsisdn("9876543210");
        mockNotification.setRead(false);
    }

    @Test
    void testGetParentNotification() {
        // Given
        String parentMsisdn = "9876543210";
        List<AdminNotification> notifications = new ArrayList<>();
        notifications.add(mockNotification);

        when(adminNotificationRepository.findByReceiverMsisdnAndIsRead(anyString(), eq(false)))
                .thenReturn(notifications);

        // When
        List<AdminNotification> result = notificationController.getParentNotification(parentMsisdn);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(mockNotification.getMessage(), result.get(0).getMessage());
        verify(adminNotificationRepository, times(100)).findByReceiverMsisdnAndIsRead(eq(parentMsisdn), eq(false));
    }

    @Test
    void testGetParentNotificationNoNotifications() {
        // Given
        String parentMsisdn = "1234567890";
        when(adminNotificationRepository.findByReceiverMsisdnAndIsRead(anyString(), eq(false)))
                .thenReturn(new ArrayList<>());

        // When
        List<AdminNotification> result = notificationController.getParentNotification(parentMsisdn);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(adminNotificationRepository).findByReceiverMsisdnAndIsRead(eq(parentMsisdn), eq(false));
    }

    @Test
    void testGetParentNotificationUnreadCount() {
        // Given
        String parentMsisdn = "9876543210";
        List<AdminNotification> notifications = new ArrayList<>();
        notifications.add(mockNotification);
        notifications.add(new AdminNotification()); // Add another notification

        when(adminNotificationRepository.findByReceiverMsisdnAndIsRead(anyString(), eq(false)))
                .thenReturn(notifications);

        // When
        Integer count = notificationController.getParentNotificationUnreadCount(parentMsisdn);

        // Then
        assertNotNull(count);
        assertEquals(2, count.intValue());
        verify(adminNotificationRepository).findByReceiverMsisdnAndIsRead(eq(parentMsisdn), eq(false));
    }

    @Test
    void testGetParentNotificationUnreadCountNoNotifications() {
        // Given
        String parentMsisdn = "1234567890";
        when(adminNotificationRepository.findByReceiverMsisdnAndIsRead(anyString(), eq(false)))
                .thenReturn(new ArrayList<>());

        // When
        Integer count = notificationController.getParentNotificationUnreadCount(parentMsisdn);

        // Then
        assertNotNull(count);
        assertEquals(0, count.intValue());
        verify(adminNotificationRepository).findByReceiverMsisdnAndIsRead(eq(parentMsisdn), eq(false));
    }

    @Test
    void testGetParentNotificationWithNullMsisdn() {
        // Given
        String parentMsisdn = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            notificationController.getParentNotification(null);
        });
        verify(adminNotificationRepository, never()).findByReceiverMsisdnAndIsRead(anyString(), eq(false));
    }

    @Test
    void testGetParentNotificationUnreadCountWithNullMsisdn() {
        // Given
        String parentMsisdn = null;

        // When & Then
        assertThrows(Exception.class, () -> {
            notificationController.getParentNotificationUnreadCount(parentMsisdn);
        });
        verify(adminNotificationRepository, never()).findByReceiverMsisdnAndIsRead(anyString(), eq(false));
    }
}
