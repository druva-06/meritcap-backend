package com.consultancy.education.model;

import com.consultancy.education.enums.GraduationLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_education")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentEducation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    GraduationLevel educationLevel;

    @Column(name = "institution_name", nullable = false)
    String institutionName;

    @Column(name = "board", nullable = false)
    String board;

    @Column(name = "college_code")
    String collegeCode;

    @Column(name = "institution_address", nullable = false)
    String institutionAddress;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Column(name = "start_year", nullable = false)
    LocalDate startYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Column(name = "end_year", nullable = false)
    LocalDate endYear;

    @Column(name = "percentage", nullable = false)
    Double percentage;

    @Column(name = "cgpa", nullable = false)
    Double cgpa;

    @Column(name = "division")
    String division;

    @Column(name = "specialization", nullable = false)
    String specialization;

    @Column(name = "backlogs", nullable = false)
    Integer backlogs;

    @Column(name = "certificate")
    String certificate;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @JoinColumn
    @ManyToOne
    Student student;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
