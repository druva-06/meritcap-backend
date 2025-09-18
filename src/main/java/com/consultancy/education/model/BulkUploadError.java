package com.consultancy.education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_upload_errors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BulkUploadError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "file_name", length = 255)
    String fileName;

    @Column(name = "row_num")
    Integer rowNumber;

    @Column(name = "entity_type", length = 50)
    String entityType; // college | course | college_course | parse | file

    @Column(name = "identifier", length = 255)
    String identifier;

    @Lob
    @Column(name = "raw_data")
    String rawData;

    @Lob
    @Column(name = "error_message", nullable = false)
    String errorMessage;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
