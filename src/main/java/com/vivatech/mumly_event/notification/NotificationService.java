package com.vivatech.mumly_event.notification;
import com.vivatech.mumly_event.model.*;

import java.time.LocalDateTime;
import java.util.List;

import com.vivatech.mumly_event.helper.MumlyEnums.NotificationType;

import com.vivatech.mumly_event.exception.CustomExceptionHandler;
import com.vivatech.mumly_event.helper.MumlyEnums;
import com.vivatech.mumly_event.notification.model.AdminNotification;
import com.vivatech.mumly_event.notification.repository.AdminNotificationRepository;
import com.vivatech.mumly_event.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    @Autowired
    private EventFeedbackRepository eventFeedbackRepository;
    @Autowired
    private MumlyEventRepository mumlyEventRepository;

    @Autowired
    private AdminNotificationRepository adminNotificationRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private MumlyEventPaymentRepository mumlyEventPaymentRepository;

    public void sendAdminNotification(Integer id, MumlyEnums.NotificationType type, String message) {
        if (type.equals(MumlyEnums.NotificationType.REGISTRATION)) {
            createRegistrationNotification(id);
        } else if (type.equals(MumlyEnums.NotificationType.PAYMENT)) {
            createPaymentNotification(id);
        } else if (type.equals(MumlyEnums.NotificationType.EMERGENCY)) {
            sendEmergencyNotification(id, message);
        } else if (type.equals(NotificationType.FEEDBACK)) {
            sendFeedbackToParent(id, message);
        }
    }

    public void createRegistrationNotification(Integer eventId) {

        EventRegistration eventRegistration = eventRegistrationRepository.findById(eventId).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));

        AdminNotification adminNotification = new AdminNotification();
        adminNotification.setMessage(eventRegistration.getParticipantName()
                + " has registered for "
                + eventRegistration.getSelectedEvent().getEventName());
        adminNotification.setType(NotificationType.REGISTRATION.toString());
        adminNotification.setRead(false);
        adminNotification.setSenderMsisdn(eventRegistration.getParticipantPhone());
        adminNotification.setReceiverMsisdn(eventRegistration.getSelectedEvent().getOrganizerPhoneNumber());
        adminNotification.setSenderEmil(eventRegistration.getParticipantEmail());
        adminNotification.setReceiverEmail(eventRegistration.getSelectedEvent().getOrganizerContactEmail());
        adminNotification.setEmailSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        adminNotification.setSmsSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        adminNotification.setRetryCount(0);
        adminNotification.setCreatedAt(LocalDateTime.now());
        adminNotification.setOrganizerId(eventRegistration.getSelectedEvent().getCreatedBy().getId());
        adminNotification.setApplicationName(MumlyEnums.ApplicationName.MUMLY_EVENT.toString());
        adminNotification.setOwnerName(eventRegistration.getSelectedEvent().getOrganizerName());
        adminNotificationRepository.save(adminNotification);
    }

    public void createPaymentNotification(Integer paymentId) {
        MumlyEventPayment mumlyEventPayment = mumlyEventPaymentRepository.findById(paymentId).orElseThrow(() -> new CustomExceptionHandler("Payment not found"));
        EventRegistration eventRegistration = mumlyEventPayment.getEventRegistration();
        if (mumlyEventPayment.getEventRegistration().getParticipantName() == null) {
            eventRegistration = eventRegistrationRepository.findById(mumlyEventPayment.getEventRegistration().getId()).orElseThrow(() -> new CustomExceptionHandler("Event registration not found"));
        }
        AdminNotification adminNotification = new AdminNotification();
        adminNotification.setMessage(eventRegistration.getParticipantName()
                + " has paid for " + eventRegistration.getSelectedEvent().getEventName() + "."
                + " Payment status: " + mumlyEventPayment.getPaymentStatus());
        adminNotification.setRead(false);
        adminNotification.setType(MumlyEnums.NotificationType.PAYMENT.toString());
        adminNotification.setSenderMsisdn(eventRegistration.getParticipantPhone());
        adminNotification.setReceiverMsisdn(eventRegistration.getSelectedEvent().getOrganizerPhoneNumber());
        adminNotification.setSenderEmil(eventRegistration.getParticipantEmail());
        adminNotification.setReceiverEmail(eventRegistration.getSelectedEvent().getOrganizerContactEmail());
        adminNotification.setEmailSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        adminNotification.setSmsSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        adminNotification.setRetryCount(0);
        adminNotification.setCreatedAt(LocalDateTime.now());
        adminNotification.setOrganizerId(eventRegistration.getSelectedEvent().getCreatedBy().getId());
        adminNotification.setApplicationName(MumlyEnums.ApplicationName.MUMLY_EVENT.toString());
        adminNotification.setOwnerName(eventRegistration.getSelectedEvent().getOrganizerName());
        adminNotificationRepository.save(adminNotification);
    }

    public void sendEmergencyNotification(Integer eventId, String message) {
        MumlyEvent event = mumlyEventRepository.findById(eventId).orElseThrow(() -> new CustomExceptionHandler("Event not found"));
        List<EventRegistration> participants = eventRegistrationRepository.findBySelectedEvent(event)
                .stream()
                .filter(ele -> ele.getStatus().equalsIgnoreCase(MumlyEnums.EventStatus.APPROVE.toString()))
                .toList();
        for (EventRegistration participant : participants) {
            AdminNotification notification = new AdminNotification();
            notification.setMessage(message);
            notification.setType(NotificationType.EMERGENCY.toString());
            notification.setRead(false);
            notification.setSenderMsisdn(participant.getSelectedEvent().getOrganizerPhoneNumber());
            notification.setReceiverMsisdn(participant.getParticipantPhone());
            notification.setSenderEmil(participant.getSelectedEvent().getOrganizerContactEmail());
            notification.setReceiverEmail(participant.getParticipantEmail());
            notification.setEmailSentStatus(MumlyEnums.EventStatus.PENDING.toString());
            notification.setSmsSentStatus(MumlyEnums.EventStatus.PENDING.toString());
            notification.setRetryCount(0);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setOrganizerId(participant.getSelectedEvent().getCreatedBy().getId());
            notification.setApplicationName(MumlyEnums.ApplicationName.MUMLY_EVENT.toString());
            notification.setOwnerName(participant.getSelectedEvent().getOrganizerName());
            adminNotificationRepository.save(notification);
        }
        log.info("EMERGENCY NOTIFICATION sent to {} participants", participants.size());
    }

    public void sendEmergencyFeedbackToSelectedParent(List<Integer> participantIds, String message) {
        List<EventRegistration> registrations = eventRegistrationRepository.findAllById(participantIds).stream()
                .filter(ele -> ele.getStatus().equalsIgnoreCase(MumlyEnums.EventStatus.APPROVE.toString()))
                .toList();;
        for (EventRegistration participant : registrations) {
            AdminNotification notification = new AdminNotification();
            notification.setMessage(message);
            notification.setType(NotificationType.EMERGENCY.toString());
            notification.setRead(false);
            notification.setSenderMsisdn(participant.getSelectedEvent().getOrganizerPhoneNumber());
            notification.setReceiverMsisdn(participant.getParticipantPhone());
            notification.setSenderEmil(participant.getSelectedEvent().getOrganizerContactEmail());
            notification.setReceiverEmail(participant.getParticipantEmail());
            notification.setEmailSentStatus(MumlyEnums.EventStatus.PENDING.toString());
            notification.setSmsSentStatus(MumlyEnums.EventStatus.PENDING.toString());
            notification.setRetryCount(0);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setOrganizerId(participant.getSelectedEvent().getCreatedBy().getId());
            notification.setApplicationName(MumlyEnums.ApplicationName.MUMLY_EVENT.toString());
            notification.setOwnerName(participant.getSelectedEvent().getOrganizerName());
            adminNotificationRepository.save(notification);
        }
    }

    private void sendFeedbackToParent(Integer id, String message) {
        EventFeedback feedback = eventFeedbackRepository.findById(id).orElseThrow(() -> new CustomExceptionHandler("Feedback not found"));
        AdminNotification notification = new AdminNotification();
        notification.setMessage(message);
        notification.setType(NotificationType.FEEDBACK.toString());
        notification.setRead(false);
        notification.setSenderMsisdn(feedback.getEventRegistration().getSelectedEvent().getOrganizerPhoneNumber());
        notification.setReceiverMsisdn(feedback.getEventRegistration().getParticipantPhone());
        notification.setSenderEmil(feedback.getEventRegistration().getSelectedEvent().getOrganizerContactEmail());
        notification.setReceiverEmail(feedback.getEventRegistration().getParticipantEmail());
        notification.setEmailSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        notification.setSmsSentStatus(MumlyEnums.EventStatus.PENDING.toString());
        notification.setRetryCount(0);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setOrganizerId(feedback.getEventRegistration().getSelectedEvent().getCreatedBy().getId());
        notification.setApplicationName(MumlyEnums.ApplicationName.MUMLY_EVENT.toString());
        notification.setOwnerName(feedback.getEventRegistration().getSelectedEvent().getOrganizerName());
        adminNotificationRepository.save(notification);
    }
}
