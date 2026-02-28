package com.consultancy.education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    String name; // e.g., "LEAD_CREATE", "LEAD_VIEW_ALL", "USER_MANAGE"

    @Column(name = "display_name", nullable = false, length = 100)
    String displayName; // Human-readable: "Create Leads", "View All Leads"

    @Column(name = "description", length = 500)
    String description;

    @Column(name = "category", length = 50)
    String category; // e.g., "LEADS", "USERS", "APPLICATIONS", "REPORTS"

    @Column(name = "dashboard", length = 100)
    String dashboard; // Top-level: "Leads", "Colleges", "Students"

    @Column(name = "submenu", length = 100)
    String submenu; // Second-level: "All Colleges", "Lead Management" (nullable)

    @Column(name = "feature", length = 100)
    String feature; // Action: "View", "Create", "Edit", "Delete"

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    Set<Role> roles = new HashSet<>();

    @ManyToMany(mappedBy = "additionalPermissions")
    @Builder.Default
    Set<User> users = new HashSet<>();

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
