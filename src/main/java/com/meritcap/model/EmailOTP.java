package com.meritcap.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_otp")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EmailOTP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "otp", nullable = false, length = 6)
    String otp;

    @Column(name = "expires_at", nullable = false)
    LocalDateTime expiresAt;

    @Column(name = "consumed", nullable = false)
    @Builder.Default
    Boolean consumed = false;

    @Column(name = "attempts", nullable = false)
    @Builder.Default
    Integer attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (consumed == null) {
            consumed = false;
        }
        if (attempts == null) {
            attempts = 0;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !consumed && !isExpired() && attempts < 5;
    }
}
