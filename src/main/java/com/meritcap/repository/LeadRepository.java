package com.meritcap.repository;

import com.meritcap.enums.LeadStatus;
import com.meritcap.model.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByEmail(String email);

    Optional<Lead> findByPhoneNumber(String phoneNumber);

    List<Lead> findByEmailOrPhoneNumber(String email, String phoneNumber);

    List<Lead> findByAssignedToId(Long assignedToId);

    List<Lead> findByCreatedById(Long createdById);

    List<Lead> findByAssignedToIsNull();

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Long countByStatus(LeadStatus status);

    @Modifying
    @Query("UPDATE Lead l SET l.assignedTo = null WHERE l.assignedTo.id = :userId")
    int clearAssignedToReferences(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Lead l SET l.createdBy = null WHERE l.createdBy.id = :userId")
    int clearCreatedByReferences(@Param("userId") Long userId);
}
