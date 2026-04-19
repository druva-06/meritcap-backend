package com.meritcap.repository;

import com.meritcap.model.LeadAssignmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadAssignmentHistoryRepository extends JpaRepository<LeadAssignmentHistory, Long> {

    List<LeadAssignmentHistory> findByLeadIdOrderByAssignedAtDesc(Long leadId);
}
