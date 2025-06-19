package com.vivatech.mumly_event.notification.repository;

import com.vivatech.mumly_event.notification.model.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Integer> {

    List<AdminNotification> findByOrganizerId(Integer organizerId);
    List<AdminNotification> findByOrganizerIdAndIsRead(Integer organizerId, Boolean isRead);

    List<AdminNotification> findByReceiverMsisdnAndIsRead(String parentMsisdn, boolean isRead);

    List<AdminNotification> findByOrganizerIdAndIsReadAndApplicationName(Integer organizerId, Boolean isRead, String applicationName);
}
