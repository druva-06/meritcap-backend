package com.meritcap.model;

import com.meritcap.enums.Month;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.BatchSize;
import java.time.LocalDateTime;
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
    @CollectionTable(name = "course_intake_months", joinColumns = @JoinColumn(name = "college_course_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "intake_months", nullable = false)
    @BatchSize(size = 500)
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

    // ---- Rich content fields (from Excel columns AN-AZ) ----

    @Column(name = "credits", columnDefinition = "TEXT")
    String credits;

    @Column(name = "detailed_scholarship_info", columnDefinition = "TEXT")
    String detailedScholarshipInfo;

    @Column(name = "why_choose_this_course", columnDefinition = "TEXT")
    String whyChooseThisCourse;

    @Column(name = "about_course", columnDefinition = "TEXT")
    String aboutCourse;

    @Column(name = "key_features", columnDefinition = "TEXT")
    String keyFeatures;

    @Column(name = "learning_outcomes", columnDefinition = "TEXT")
    String learningOutcomes;

    @Column(name = "course_highlights", columnDefinition = "TEXT")
    String courseHighlights;

    @Column(name = "career_opportunity", columnDefinition = "TEXT")
    String careerOpportunity;

    @Column(name = "faqs_course", columnDefinition = "TEXT")
    String faqsCourse;

    @Column(name = "core_modules", columnDefinition = "TEXT")
    String coreModules;

    @Column(name = "assessment_methods", columnDefinition = "TEXT")
    String assessmentMethods;

    @Column(name = "job_markets", columnDefinition = "TEXT")
    String jobMarkets;

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
