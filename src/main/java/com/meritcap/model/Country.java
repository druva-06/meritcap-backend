package com.meritcap.model;

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

@Entity
@Table(name = "countries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_countries_name", columnNames = {"name"}),
        @UniqueConstraint(name = "uk_countries_code", columnNames = {"code"})
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE countries SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Country {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    String name;

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    String code;

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
        if (name != null) name = name.trim().replaceAll("\\s+", " ");
        if (code != null) code = code.trim().toUpperCase();
    }
}
