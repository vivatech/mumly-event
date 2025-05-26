package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mumly_event_organizer")
public class MumlyEventOrganizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "organizer_name", nullable = false, length = 100)
    private String organizerName;

    @Column(length = 20)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String website;

    @Column(name = "tax_identification_number", length = 50)
    private String taxIdentificationNumber;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "identification_file", length = 50)
    private String identificationFile;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(name = "age_group", length = 50)
    private String ageGroup;

    @Column(name = "event_format", length = 20)
    private String eventFormat;

    @Column(name = "experience_description", columnDefinition = "TEXT")
    private String experienceDescription;

    @Column(name = "child_safety_certificate", length = 50)
    private String childSafetyCertificate;

    @Column(name = "preferred_payout_method", length = 50)
    private String preferredPayoutMethod;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "admin_id", nullable = false)
    private Integer adminId;
}
