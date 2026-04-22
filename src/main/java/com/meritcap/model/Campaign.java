package com.meritcap.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "campaigns")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Column(name = "source")
    String source;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "qr_code", length = 512)
    String qrCode;

    @Column(name = "status", nullable = false)
    @Builder.Default
    String status = "ACTIVE";

    @Column(name = "created_by")
    String createdBy;

    @Column(name = "created_at", nullable = false)
    LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (status == null) {
            status = "ACTIVE";
        }
    }
}
