package com.consultancy.education.repository.specification;

import com.consultancy.education.DTOs.requestDTOs.lead.LeadFilterDto;
import com.consultancy.education.enums.LeadStatus;
import com.consultancy.education.model.Lead;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class LeadSpecification {

    public static Specification<Lead> filterLeads(LeadFilterDto filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search by name, email, or phone
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), searchPattern)
                );
                predicates.add(searchPredicate);
            }

            // Filter by campaign
            if (filter.getCampaign() != null && !filter.getCampaign().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("campaign"), filter.getCampaign()));
            }

            // Filter by date range (created_at)
            if (filter.getDateFrom() != null) {
                LocalDateTime startOfDay = filter.getDateFrom().atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startOfDay));
            }
            
            if (filter.getDateTo() != null) {
                LocalDateTime endOfDay = filter.getDateTo().atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endOfDay));
            }

            // Filter by score range
            if (filter.getScoreFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("score"), filter.getScoreFrom()));
            }
            
            if (filter.getScoreTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("score"), filter.getScoreTo()));
            }

            // Filter by status list
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                predicates.add(root.get("status").in(filter.getStatus()));
            }

            // Filter by tags
            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                List<Predicate> tagPredicates = new ArrayList<>();
                for (String tag : filter.getTags()) {
                    String tagPattern = "%" + tag + "%";
                    tagPredicates.add(criteriaBuilder.like(root.get("tags"), tagPattern));
                }
                predicates.add(criteriaBuilder.or(tagPredicates.toArray(new Predicate[0])));
            }

            // Filter by assigned counselor
            if (filter.getAssignedTo() != null) {
                predicates.add(criteriaBuilder.equal(root.get("assignedTo").get("id"), filter.getAssignedTo()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
