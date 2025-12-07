package com.consultancy.education.repository;

import com.consultancy.education.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    Optional<Lead> findByEmail(String email);

    Optional<Lead> findByPhoneNumber(String phoneNumber);

    List<Lead> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<Lead> findByAssignedToId(Long assignedToId);

    List<Lead> findByCreatedById(Long createdById);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
