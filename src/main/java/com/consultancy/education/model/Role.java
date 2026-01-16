package com.consultancy.education.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", unique = true, nullable = false, length = 50)
    String name; // ADMIN, COUNSELOR, STUDENT, COLLEGE, SUB_AGENT, etc.

    @Column(name = "display_name", nullable = false, length = 100)
    String displayName; // Human-readable name: "Administrator", "Counselor", etc.

    @Column(name = "description", length = 500)
    String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    Boolean isSystemRole = false; // System roles cannot be deleted

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"), inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    Set<Permission> permissions = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "created_by")
    Long createdBy;

    @Column(name = "updated_by")
    Long updatedBy;

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
