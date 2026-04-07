package com.meritcap.model;

import com.meritcap.enums.DocumentCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Master registry of document types.
 * Named DocumentTypeEntity to avoid collision with the existing DocumentType enum.
 * The {@code code} field is the bridge to {@code documents.document_type} (plain string).
 */
@Entity
@Table(name = "document_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_document_types_code", columnNames = {"code"})
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE document_types SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    String name;

    /**
     * Machine-friendly unique key. Must match the string value stored in
     * {@code documents.document_type} for compliance checking to work.
     * Examples: PASSPORT, SOP, LOR, TRANSCRIPT
     */
    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    String code;

    @Size(max = 500)
    @Column(length = 500)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    DocumentCategory category;

    @Builder.Default
    @Column(name = "allow_multiple", nullable = false)
    Boolean allowMultiple = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    String updatedBy;

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (code != null) code = code.trim().toUpperCase().replaceAll("\\s+", "_");
        if (name != null) name = name.trim().replaceAll("\\s+", " ");
    }
}
