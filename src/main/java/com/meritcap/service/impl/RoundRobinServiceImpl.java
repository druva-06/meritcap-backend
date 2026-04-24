package com.meritcap.service.impl;

import com.meritcap.DTOs.responseDTOs.lead.AssignmentHistoryDto;
import com.meritcap.DTOs.responseDTOs.lead.CounselorWorkloadDto;
import com.meritcap.DTOs.responseDTOs.lead.DirectAssignResultDto;
import com.meritcap.DTOs.responseDTOs.lead.LeadResponseDto;
import com.meritcap.DTOs.responseDTOs.lead.RoundRobinAssignResultDto;
import com.meritcap.enums.LeadStatus;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Lead;
import com.meritcap.model.LeadAssignmentHistory;
import com.meritcap.model.RoundRobinState;
import com.meritcap.model.User;
import com.meritcap.repository.LeadAssignmentHistoryRepository;
import com.meritcap.repository.LeadRepository;
import com.meritcap.repository.RoundRobinStateRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.service.RoundRobinService;
import com.meritcap.transformer.LeadTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoundRobinServiceImpl implements RoundRobinService {

    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final RoundRobinStateRepository roundRobinStateRepository;
    private final LeadAssignmentHistoryRepository assignmentHistoryRepository;

    public RoundRobinServiceImpl(
            LeadRepository leadRepository,
            UserRepository userRepository,
            RoundRobinStateRepository roundRobinStateRepository,
            LeadAssignmentHistoryRepository assignmentHistoryRepository) {
        this.leadRepository = leadRepository;
        this.userRepository = userRepository;
        this.roundRobinStateRepository = roundRobinStateRepository;
        this.assignmentHistoryRepository = assignmentHistoryRepository;
    }

    @Override
    @Transactional
    public RoundRobinAssignResultDto assignLeadsRoundRobin(List<Long> leadIds, String adminEmail) {
        log.info("Round-robin assignment triggered by: {}", adminEmail);

        List<User> counselors = getActiveCounselors();
        if (counselors.isEmpty()) {
            log.warn("No active counselors found for round-robin assignment");
            return new RoundRobinAssignResultDto(0, Collections.emptyMap());
        }

        List<Lead> leadsToAssign = resolveLeads(leadIds);
        if (leadsToAssign.isEmpty()) {
            log.info("No leads to assign");
            return new RoundRobinAssignResultDto(0, Collections.emptyMap());
        }

        User assignedBy = userRepository.findByEmail(adminEmail);

        // Build sorted rotation queue: counselors who haven't been assigned recently go first
        List<RoundRobinState> states = ensureStatesExist(counselors);
        states.sort(Comparator
                .comparing((RoundRobinState s) -> s.getLastAssignedAt() == null ? LocalDateTime.MIN : s.getLastAssignedAt())
                .thenComparingInt(RoundRobinState::getAssignmentCount));

        Map<Long, RoundRobinState> stateMap = states.stream()
                .collect(Collectors.toMap(s -> s.getCounselor().getId(), s -> s));

        Map<String, Integer> assignmentResult = new LinkedHashMap<>();
        int n = counselors.size();

        for (int i = 0; i < leadsToAssign.size(); i++) {
            Lead lead = leadsToAssign.get(i);
            // Round-robin index rotates through the sorted counselor list
            User counselor = states.get(i % n).getCounselor();

            LeadAssignmentHistory history = new LeadAssignmentHistory();
            history.setLead(lead);
            history.setFromCounselor(lead.getAssignedTo());
            history.setToCounselor(counselor);
            history.setAssignedBy(assignedBy);
            history.setReason("Round-robin auto-assignment");
            assignmentHistoryRepository.save(history);

            lead.setAssignedTo(counselor);
            leadRepository.save(lead);

            RoundRobinState state = stateMap.get(counselor.getId());
            state.setLastAssignedAt(LocalDateTime.now());
            state.setAssignmentCount(state.getAssignmentCount() + 1);
            roundRobinStateRepository.save(state);

            String counselorName = counselor.getFirstName() + " " + counselor.getLastName();
            assignmentResult.merge(counselorName, 1, Integer::sum);
        }

        log.info("Round-robin complete: {} leads assigned across {} counselors", leadsToAssign.size(), n);
        return new RoundRobinAssignResultDto(leadsToAssign.size(), assignmentResult);
    }

    @Override
    @Transactional
    public DirectAssignResultDto directAssignLeads(
            String campaignName, Long counselorId, int count, String sortBy, String adminEmail) {

        User counselor = userRepository.findById(counselorId)
                .orElseThrow(() -> new NotFoundException("Counselor not found: " + counselorId));
        User assignedBy = userRepository.findByEmail(adminEmail);

        List<Lead> candidates = (campaignName != null && !campaignName.isBlank())
                ? leadRepository.findByCampaignAndAssignedToIsNull(campaignName)
                : leadRepository.findByAssignedToIsNull();

        if ("score".equals(sortBy)) {
            candidates.sort(Comparator.comparingInt(
                (Lead l) -> l.getScore() != null ? l.getScore() : 0).reversed());
        } else {
            candidates.sort(Comparator.comparing(
                l -> l.getCreatedAt() != null ? l.getCreatedAt() : LocalDateTime.MIN));
        }

        int effectiveCount = Math.min(count, candidates.size());
        List<Lead> toAssign = candidates.subList(0, effectiveCount);
        String counselorFullName = counselor.getFirstName() + " " + counselor.getLastName();

        for (Lead lead : toAssign) {
            LeadAssignmentHistory history = new LeadAssignmentHistory();
            history.setLead(lead);
            history.setFromCounselor(lead.getAssignedTo());
            history.setToCounselor(counselor);
            history.setAssignedBy(assignedBy);
            history.setReason("Direct assignment by admin");
            assignmentHistoryRepository.save(history);

            lead.setAssignedTo(counselor);
            leadRepository.save(lead);
        }

        if (effectiveCount > 0) {
            RoundRobinState state = roundRobinStateRepository.findByCounselorId(counselorId)
                    .orElse(new RoundRobinState(counselor));
            state.setLastAssignedAt(LocalDateTime.now());
            state.setAssignmentCount(state.getAssignmentCount() + effectiveCount);
            roundRobinStateRepository.save(state);
        }

        log.info("Direct assignment: {} leads assigned to counselor {} from campaign '{}'",
                effectiveCount, counselorId, campaignName);

        return new DirectAssignResultDto(effectiveCount, count, counselorFullName, campaignName);
    }

    @Override
    @Transactional
    public LeadResponseDto reassignLead(Long leadId, Long newCounselorId, String reason, String byEmail) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new NotFoundException("Lead not found: " + leadId));

        User newCounselor = userRepository.findById(newCounselorId)
                .orElseThrow(() -> new NotFoundException("Counselor not found: " + newCounselorId));

        User assignedBy = userRepository.findByEmail(byEmail);

        LeadAssignmentHistory history = new LeadAssignmentHistory();
        history.setLead(lead);
        history.setFromCounselor(lead.getAssignedTo());
        history.setToCounselor(newCounselor);
        history.setAssignedBy(assignedBy);
        history.setReason(reason);
        assignmentHistoryRepository.save(history);

        lead.setAssignedTo(newCounselor);
        leadRepository.save(lead);

        // Update round-robin state for new counselor
        RoundRobinState state = roundRobinStateRepository.findByCounselorId(newCounselorId)
                .orElse(new RoundRobinState(newCounselor));
        state.setLastAssignedAt(LocalDateTime.now());
        state.setAssignmentCount(state.getAssignmentCount() + 1);
        roundRobinStateRepository.save(state);

        log.info("Lead {} reassigned to counselor {}", leadId, newCounselorId);
        return LeadTransformer.toResponseDto(lead);
    }

    @Override
    public List<CounselorWorkloadDto> getCounselorWorkload() {
        List<User> counselors = getActiveCounselors();
        Map<Long, RoundRobinState> stateMap = roundRobinStateRepository.findAll().stream()
                .collect(Collectors.toMap(s -> s.getCounselor().getId(), s -> s));

        return counselors.stream().map(counselor -> {
            List<Lead> leads = leadRepository.findByAssignedToId(counselor.getId());

            int hot = 0, immediateHot = 0, warm = 0, cold = 0, feature = 0, contacted = 0;
            for (Lead lead : leads) {
                switch (lead.getStatus()) {
                    case HOT -> hot++;
                    case IMMEDIATE_HOT -> immediateHot++;
                    case WARM -> warm++;
                    case COLD -> cold++;
                    case FEATURE_LEAD -> feature++;
                    case CONTACTED -> contacted++;
                }
            }

            RoundRobinState state = stateMap.get(counselor.getId());
            LocalDateTime lastAssignedAt = state != null ? state.getLastAssignedAt() : null;

            return new CounselorWorkloadDto(
                    counselor.getId(),
                    counselor.getFirstName() + " " + counselor.getLastName(),
                    counselor.getEmail(),
                    leads.size(),
                    hot, immediateHot, warm, cold, feature, contacted,
                    lastAssignedAt
            );
        }).sorted(Comparator.comparingInt(CounselorWorkloadDto::getTotalLeads).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<AssignmentHistoryDto> getLeadAssignmentHistory(Long leadId) {
        return assignmentHistoryRepository.findByLeadIdOrderByAssignedAtDesc(leadId).stream()
                .map(h -> new AssignmentHistoryDto(
                        h.getId(),
                        h.getFromCounselor() != null
                                ? h.getFromCounselor().getFirstName() + " " + h.getFromCounselor().getLastName()
                                : null,
                        h.getToCounselor() != null
                                ? h.getToCounselor().getFirstName() + " " + h.getToCounselor().getLastName()
                                : null,
                        h.getAssignedBy() != null
                                ? h.getAssignedBy().getFirstName() + " " + h.getAssignedBy().getLastName()
                                : "System",
                        h.getReason(),
                        h.getAssignedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void autoAssignNewStudentLead(User user) {
        // Skip if a lead already exists for this email (idempotent)
        if (leadRepository.findByEmail(user.getEmail()).isPresent()) {
            log.info("Lead already exists for {}, skipping auto-assignment", user.getEmail());
            return;
        }

        Lead lead = new Lead();
        lead.setFirstName(user.getFirstName());
        lead.setLastName(user.getLastName() != null ? user.getLastName() : "");
        lead.setEmail(user.getEmail());
        lead.setPhoneNumber(user.getPhoneNumber());
        lead.setStatus(LeadStatus.WARM);
        lead.setScore(0);
        lead.setLeadSource("Website Signup");
        lead.setIsDuplicate(false);
        lead.setCreatedBy(null);
        leadRepository.save(lead);

        log.info("Auto-created lead for new student: {}", user.getEmail());

        assignLeadsRoundRobin(List.of(lead.getId()), "system");
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private List<User> getActiveCounselors() {
        return userRepository.findByRoleName("COUNSELOR").stream()
                .filter(u -> !Boolean.TRUE.equals(u.getAccountLocked()))
                .collect(Collectors.toList());
    }

    private List<Lead> resolveLeads(List<Long> leadIds) {
        if (leadIds == null || leadIds.isEmpty()) {
            return leadRepository.findByAssignedToIsNull();
        }
        return leadRepository.findAllById(leadIds).stream()
                .filter(l -> l.getAssignedTo() == null)
                .collect(Collectors.toList());
    }

    private List<RoundRobinState> ensureStatesExist(List<User> counselors) {
        List<RoundRobinState> states = new ArrayList<>();
        for (User counselor : counselors) {
            RoundRobinState state = roundRobinStateRepository.findByCounselorId(counselor.getId())
                    .orElseGet(() -> {
                        RoundRobinState newState = new RoundRobinState(counselor);
                        return roundRobinStateRepository.save(newState);
                    });
            states.add(state);
        }
        return states;
    }
}
