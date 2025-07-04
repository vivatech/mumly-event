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

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "organizer_name", nullable = false, length = 100)
    private String organizerName;

    @Column(length = 20)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(name = "is_email_verified", length = 3)
    private String isEmailVerified; // or use Enum YesNo

    @Column(length = 20)
    private String mobile;

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
    private String activityInfo;

    @Column(length = 250)
    private String availability;

    @Column(name = "event_format", length = 20)
    private String eventFormat;

    @Column(name = "year_of_experience")
    private Integer yearOfExperience;

    @Column(name = "experience_description", columnDefinition = "TEXT")
    private String experienceDescription;

    @Column(name = "child_safety_certificate", length = 50)
    private String childSafetyCertificate;

    @Column(name = "is_physical_space", length = 3)
    private String isPhysicalSpace; // or use Enum YesNo

    @Column(name = "specification_child_safety", length = 250)
    private String specificationChildSafety;

    @Column(name = "preferred_payout_method", length = 50)
    private String preferredPayoutMethod;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "national_id_file", length = 50)
    private String nationalIdFile;

    @Column(name = "portfolio_file", length = 50)
    private String portfolioFile;

    @Column(name = "reference_letter_file", length = 50)
    private String referenceLetterFile;

    @Column(name = "status", length = 8)
    private String status; // or use Enum (ACTIVE, INACTIVE)

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String activities;

    @Column(name = "admin_id", nullable = false)
    private Integer adminId;

    @Column(name = "age_group", length = 50)
    private String ageGroup;

    @Column(name = "approval_status", length = 10)
    private String approvalStatus; // or use Enum (Pending, Approved, Rejected)

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "approved_by")
    private Integer approvedBy;
}
