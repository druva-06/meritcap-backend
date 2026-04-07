package com.meritcap.model;

import com.meritcap.enums.ActiveStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.validator.constraints.URL;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "colleges", indexes = {
                @Index(name = "idx_colleges_name", columnList = "name"),
                @Index(name = "idx_colleges_country", columnList = "country"),
                @Index(name = "idx_colleges_established_year", columnList = "established_year"),
                @Index(name = "idx_colleges_slug", columnList = "slug")
}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_colleges_campus_code", columnNames = { "campus_code" }),
                @UniqueConstraint(name = "uk_colleges_slug", columnNames = { "slug" })
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE colleges SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class College {

        // ---------- Identity ----------
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        /** SEO-friendly handle for lookups like GET /colleges/{slug} */
        @NotBlank
        @Size(max = 160)
        @Column(name = "slug", nullable = false, length = 160)
        String slug;

        // ---------- Core fields ----------
        @NotBlank
        @Size(max = 255)
        @Column(nullable = false, length = 255)
        String name;

        @Size(max = 255)
        @Column(name = "campus_name", length = 255)
        String campusName;

        /** User-supplied, unique, immutable natural key for joins. */
        @NotBlank
        @Size(min = 3, max = 64)
        // Optionally enforce: @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Only A-Z,
        // 0-9, _, - allowed")
        @Column(name = "campus_code", unique = true, nullable = false, updatable = false, length = 64)
        String campusCode;

        @Size(max = 100)
        @Column(name = "country", length = 100)
        String country;

        @Min(1800)
        @Max(2100)
        @Column(name = "established_year")
        Integer establishedYear;

        @Size(max = 255)
        @Column(name = "ranking", length = 255)
        String ranking;

        @Lob
        @Column(name = "description", columnDefinition = "TEXT")
        String description;

        // ---------- Media / links ----------
        @URL
        @Size(max = 1024)
        @Column(name = "website_url", length = 1024)
        String websiteUrl;

        @URL
        @Size(max = 1024)
        @Column(name = "college_logo", length = 1024)
        String collegeLogo;

        @URL
        @Size(max = 1024)
        @Column(name = "campus_gallery_video_link", length = 1024)
        String campusGalleryVideoLink;

        // Optional hero image (useful for landing pages; safe no-op if unused)
        @URL
        @Size(max = 1024)
        @Column(name = "banner_url", length = 1024)
        String bannerUrl;

        // ---------- Lifecycle ----------
        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        ActiveStatus status = ActiveStatus.ACTIVE;

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

        @Builder.Default
        @Column(name = "is_deleted", nullable = false)
        Boolean isDeleted = false;

        // ---------- University-level content ----------
        @Lob
        @Column(name = "faqs_university", columnDefinition = "TEXT")
        String faqsUniversity;

        // ---------- Relations ----------
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "country_id")
        Country countryEntity;

        @OneToOne(fetch = FetchType.LAZY, optional = true, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
        @JoinColumn(name = "seo_id", referencedColumnName = "id", unique = true)
        @ToString.Exclude
        com.meritcap.model.Seo seo;

        // ---------- Normalization ----------
        @PrePersist
        @PreUpdate
        private void normalize() {
                if (slug != null)
                        slug = slug.trim().toLowerCase().replaceAll("\\s+", "-");
                if (campusCode != null)
                        campusCode = campusCode.trim().toUpperCase();
                if (name != null)
                        name = name.trim().replaceAll("\\s+", " ");
                if (campusName != null)
                        campusName = campusName.trim().replaceAll("\\s+", " ");
                if (country != null)
                        country = country.trim().replaceAll("\\s+", " ");
        }
}
