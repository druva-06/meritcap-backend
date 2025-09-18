package com.consultancy.education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_upload_job",
        indexes = {
                @Index(name = "idx_bulk_upload_job_status", columnList = "status"),
                @Index(name = "idx_bulk_upload_job_created_by", columnList = "created_by")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class BulkUploadJob implements Serializable {

    public enum Status { PENDING, IN_PROGRESS, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    String fileName;

    @Column(name = "total_records", nullable = false)
    Integer totalRecords = 0;

    @Column(name = "processed_records", nullable = false)
    Integer processedRecords = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    Status status = Status.PENDING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;

    @Column(name = "created_by", length = 100)
    String createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    Integer version;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.totalRecords == null) this.totalRecords = 0;
        if (this.processedRecords == null) this.processedRecords = 0;
        if (this.status == null) this.status = Status.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
