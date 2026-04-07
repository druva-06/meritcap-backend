package com.meritcap.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

/**
 * Defines which document types every student must upload globally
 * (i.e., regardless of the college's country) to complete their profile.
 */
@Entity
@Table(name = "profile_document_requirements", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_doc_req_type", columnNames = {"document_type_id"})
})
@SQLDelete(sql = "UPDATE profile_document_requirements SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileDocumentRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_type_id", nullable = false)
    DocumentTypeEntity documentType;

    @Builder.Default
    @Column(name = "is_required", nullable = false)
    Boolean isRequired = true;

    @Min(1)
    @Builder.Default
    @Column(name = "min_count", nullable = false)
    Integer minCount = 1;

    @Builder.Default
    @Column(name = "display_order", nullable = false)
    Integer displayOrder = 0;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

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
