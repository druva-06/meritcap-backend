package com.consultancy.education.model;

import com.consultancy.education.enums.Month;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "college_courses") // Proper table name
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CollegeCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "course_url", columnDefinition = "TEXT")
    String courseUrl;

    @Column(name = "duration", nullable = false)
    Integer duration;

    @ElementCollection(targetClass = Month.class)
    @CollectionTable(name = "course_intake_months", joinColumns = @JoinColumn(name = "course_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "intake_months", nullable = false)
    List<Month> intakeMonths;

    @Column(name = "intake_year", nullable = false)
    Integer intakeYear;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    String eligibilityCriteria;

    @Column(name = "application_fee", columnDefinition = "TEXT")
    String applicationFee;

    @Column(name = "tuition_fee", columnDefinition = "TEXT")
    String tuitionFee;

    @Column(name = "ielts_min_score")
    Double ieltsMinScore;

    @Column(name = "ielts_min_band_score")
    Double ieltsMinBandScore;

    @Column(name = "toefl_min_score")
    Double toeflMinScore;

    @Column(name = "toefl_min_band_score")
    Double toeflMinBandScore;

    @Column(name = "pte_min_score")
    Double pteMinScore;

    @Column(name = "pte_min_band_score")
    Double pteMinBandScore;

    @Column(name = "det_min_score")
    Double detMinScore;

    @Column(name = "gre_min_score")
    Double greMinScore;

    @Column(name = "gmat_min_score")
    Double gmatMinScore;

    @Column(name = "sat_min_score")
    Double satMinScore;

    @Column(name = "cat_min_score")
    Double catMinScore;

    @Column(name = "min_10th_score")
    Double min10thScore;

    @Column(name = "min_inter_score")
    Double minInterScore;

    @Column(name = "min_graduation_score")
    Double minGraduationScore;

    @Column(name = "scholarship_eligible", columnDefinition = "TEXT")
    String scholarshipEligible;

    @Column(name = "scholarship_details", columnDefinition = "TEXT")
    String scholarshipDetails;

    @Column(name = "backlog_acceptance_range", columnDefinition = "TEXT")
    String backlogAcceptanceRange;

    @Column(name = "remarks", columnDefinition = "TEXT")
    String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @JoinColumn
    @ManyToOne
    College college;

    @JoinColumn
    @ManyToOne
    Course course;

    @OneToMany(mappedBy = "collegeCourse", cascade = CascadeType.ALL, orphanRemoval = true)
    List<StudentCollegeCourseRegistration> studentCollegeCourseRegistrations = new ArrayList<>();

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
