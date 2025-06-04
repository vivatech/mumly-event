package com.vivatech.mumly_event.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "temp_mumly_event_parent_feedback")
public class ParentFeedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    private String parentName;

    @ManyToOne
    private MumlyEvent event;

    @NotNull
    @Min(1)
    @Max(5)
    private int rating;
    private String comment;

    private LocalDate feedbackDate;
    private Integer submittedById;
}
