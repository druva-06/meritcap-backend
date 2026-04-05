package com.meritcap.repository;

import com.meritcap.model.EmailOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailOTPRepository extends JpaRepository<EmailOTP, Long> {
    
    Optional<EmailOTP> findTopByEmailAndConsumedFalseOrderByCreatedAtDesc(String email);
    
    Optional<EmailOTP> findByEmailAndOtpAndConsumedFalse(String email, String otp);

    void deleteAllByEmail(String email);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
