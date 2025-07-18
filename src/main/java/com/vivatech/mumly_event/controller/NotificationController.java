package com.vivatech.mumly_event.controller;
import com.vivatech.mumly_event.model.MumlyEventOrganizer;
import com.vivatech.mumly_event.helper.MumlyEnums.NotificationType;

import com.vivatech.mumly_event.dto.AdminNotificationDto;
import com.vivatech.mumly_event.dto.Response;
import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.model.*;
import com.vivatech.mumly_event.notification.NotificationService;
import com.vivatech.mumly_event.notification.model.AdminNotification;
import com.vivatech.mumly_event.notification.repository.AdminNotificationRepository;
import com.vivatech.mumly_event.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/event/notifications")
@Tag(name = "Notification Controller", description = "Notification APIs")
public class NotificationController {

    private final AdminNotificationRepository adminNotificationRepository;
    private final MumlyEventOrganizerRepository mumlyEventOrganizerRepository;
    private final MumlyAdminsRepository mumlyAdminsRepository;
    private final NotificationService notificationService;

    public NotificationController(AdminNotificationRepository adminNotificationRepository,
                                  MumlyEventOrganizerRepository mumlyEventOrganizerRepository,
                                  MumlyAdminsRepository mumlyAdminsRepository,
                                  NotificationService notificationService) {
        this.adminNotificationRepository = adminNotificationRepository;
        this.mumlyEventOrganizerRepository = mumlyEventOrganizerRepository;
        this.mumlyAdminsRepository = mumlyAdminsRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/update-read-status/{id}")
    public Response updateReadStatus(@PathVariable Integer id) {
        AdminNotification notification = adminNotificationRepository.findById(id).orElseThrow(() -> new CustomExceptionHandler("Notification not found"));
        notification.setRead(true);
        adminNotificationRepository.save(notification);
        return Response.builder().status("SUCCESS").message("Notification marked as read").build();
    }

    @GetMapping
    public List<AdminNotification> getNotifications(@RequestParam(name = "username") String username,
                                                    @RequestParam(name = "isRead", required = false, defaultValue = "false") Boolean isRead) {
        MumlyAdmin mumlyAdmin = mumlyAdminsRepository.findByUsername(username);
        MumlyEventOrganizer organizer = mumlyEventOrganizerRepository.findByAdminId(mumlyAdmin.getId()).orElseThrow(() -> new CustomExceptionHandler("Organizer not found"));
        List<AdminNotification> adminNotification = adminNotificationRepository.findByOrganizerIdAndIsRead(organizer.getId(), isRead);
        return adminNotification.stream().filter(ele -> Arrays.asList(NotificationType.PAYMENT.toString(), NotificationType.REGISTRATION.toString()).contains(ele.getType())).toList();
    }

    @GetMapping("/parent-notification")
    public List<AdminNotification> getParentNotification(@RequestParam(required = true) String parentMsisdn) {
        if (StringUtils.isEmpty(parentMsisdn)) throw new CustomExceptionHandler("Parent msisdn is required");
        return adminNotificationRepository.findByReceiverMsisdnAndIsRead(parentMsisdn, false);
    }

    @GetMapping("/parent-notification-unread-count")
    public Integer getParentNotificationUnreadCount(@RequestParam String parentMsisdn) {
        if (StringUtils.isEmpty(parentMsisdn)) throw new CustomExceptionHandler("Parent msisdn is required");
        List<AdminNotification> parentNotification = adminNotificationRepository.findByReceiverMsisdnAndIsRead(parentMsisdn, false);
        return parentNotification.size();
    }

    @Operation(summary = "v1.3.7: Send emergency notification", description = "Send emergency notification")
    @PostMapping("/emergency-notification")
    public Response sendEmergencyNotification(@RequestBody AdminNotificationDto dto) {
        String validationMessage = dto.validate();
        if (validationMessage != null) throw new CustomExceptionHandler(validationMessage);
        if (dto.getEventId() != null && !dto.getParticipantIds().isEmpty()) throw new CustomExceptionHandler("Participant Ids or Event Id should be null");
        //After validation apply the logic
        if (dto.getEventId() != null) notificationService.sendAdminNotification(dto.getEventId(), MumlyEnums.NotificationType.EMERGENCY, dto.getMessage());
        if (!dto.getParticipantIds().isEmpty()) notificationService.sendEmergencyFeedbackToSelectedParent(dto.getParticipantIds(), dto.getMessage());
        return Response.builder().status("SUCCESS").message("Notification created successfully. Sending in the background.").build();
    }
}
