package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mumly_admins")
public class MumlyAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tenant_id", nullable = false)
    private Integer tenantId;

    @Column(name = "id_admin_group", nullable = false)
    private Integer idAdminGroup;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 70)
    private String password;

    @Column(name = "tr_pass", nullable = false, length = 200)
    private String trPass;

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    @Column(name = "middlename", nullable = false, length = 50)
    private String middlename;

    @Column(name = "lastname", length = 50)
    private String lastname;

    @Column(name = "image", nullable = false, length = 100)
    private String image;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "city", nullable = false, length = 80)
    private String city;

    @Column(name = "mobile", nullable = false, length = 20)
    private String mobile;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_time", nullable = false)
    private LocalDateTime lastLoginTime;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    @Column(name = "random_number", nullable = false, length = 20)
    private String randomNumber;

    @Column(name = "country_id", nullable = false)
    private Integer countryId;

    @Column(name = "county_id", nullable = false)
    private Integer countyId;

    @Column(name = "subcounty_id", nullable = false)
    private Integer subcountyId;

    @Column(name = "weather_division_id", nullable = false)
    private Integer weatherDivisionId;

    @Column(name = "otp", nullable = false, length = 10)
    private String otp;

    @Column(name = "created_by", nullable = false)
    private Integer createdBy;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Date updatedAt;

    @Column(name = "updated_by", nullable = false)
    private Integer updatedBy;


}
