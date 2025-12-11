package com.consultancy.education.model;

import com.consultancy.education.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Personal Information (Searchable/Filterable)
    @Column(name = "first_name", nullable = false)
    String firstName;

    @Column(name = "last_name", nullable = false)
    String lastName;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "phone_number", nullable = false)
    String phoneNumber;

    @Column(name = "country")
    String country;

    // Lead Management Fields (Searchable/Filterable)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    LeadStatus status;

    @Column(name = "score")
    Integer score;

    @Column(name = "lead_source")
    String leadSource;

    @Column(name = "campaign")
    String campaign; // Campaign name for filtering

    // Preferred options for filtering/searching
    @Column(name = "preferred_countries", columnDefinition = "TEXT")
    String preferredCountries; // Comma-separated or JSON array

    @Column(name = "preferred_courses", columnDefinition = "TEXT")
    String preferredCourses; // Comma-separated or JSON array

    @Column(name = "budget_range")
    String budgetRange;

    @Column(name = "intake")
    String intake;

    @Column(name = "tags", columnDefinition = "TEXT")
    String tags; // Comma-separated tags for quick filtering

    // Assignment
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    User assignedTo; // Counselor assigned to this lead

    @ManyToOne
    @JoinColumn(name = "created_by")
    User createdBy; // Admin who created the lead

    // Encrypted sensitive data (not used for filtering)
    @Column(name = "encrypted_personal_details", columnDefinition = "TEXT")
    String encryptedPersonalDetails; // JSON containing: alternate phone, DOB, gender, address, city, state, pincode

    @Column(name = "encrypted_academic_details", columnDefinition = "TEXT")
    String encryptedAcademicDetails; // JSON containing: education level, degree, university, CGPA, year, work
                                     // experience, test scores

    @Column(name = "encrypted_preferences", columnDefinition = "TEXT")
    String encryptedPreferences; // JSON containing: preferred college, additional notes, etc.

    // Metadata
    @Column(name = "is_duplicate")
    @Builder.Default
    Boolean isDuplicate = false;

    @Column(name = "duplicate_of")
    Long duplicateOf; // Reference to original lead ID if this is a duplicate

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LeadStatus.WARM;
        }
        if (score == null) {
            score = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
