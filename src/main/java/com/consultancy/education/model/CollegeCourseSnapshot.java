package com.consultancy.education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity
@Table(name = "college_course_snapshot")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CollegeCourseSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long courseId;
    String courseName;
    Long collegeId;
    String collegeName;

    @Column(name = "course_url", columnDefinition = "TEXT")
    String courseUrl;

    @Column(name = "duration", nullable = false)
    Integer duration;

    String intakeMonths; // e.g., "JANUARY,FEBRUARY"

    @Column(name = "intake_year", nullable = false)
    Integer intakeYear;

    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    String eligibilityCriteria;

    @Column(name = "application_fee", columnDefinition = "TEXT")
    String applicationFee;

    @Column(name = "tuition_fee", columnDefinition = "TEXT")
    String tuitionFee;

    // Test scores
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

    // Academics
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

    @Column(name = "snapshot_taken_at", nullable = false, updatable = false)
    LocalDateTime snapshotTakenAt;
}
