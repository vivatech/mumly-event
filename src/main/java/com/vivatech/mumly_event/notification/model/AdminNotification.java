package com.vivatech.mumly_event.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "temp_mumly_event_admin_notification")
public class AdminNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(columnDefinition = "TEXT")
    private String message;

    private String type; // REGISTRATION or PAYMENT

    private boolean isRead = false;

    private String senderMsisdn;
    private String receiverMsisdn;

    private String senderEmil;
    private String receiverEmail;

    private String emailSentStatus;
    private String smsSentStatus;

    @Column(name = "retry_count", nullable = false, columnDefinition = "integer default 0")
    private Integer retryCount = 0;

    private LocalDateTime createdAt;

    @Column(name = "organizer_id")
    private Integer organizerId;
    @Column(name = "application_name")
    private String applicationName;
    @Column(name = "owner_name")
    private String ownerName;
}
