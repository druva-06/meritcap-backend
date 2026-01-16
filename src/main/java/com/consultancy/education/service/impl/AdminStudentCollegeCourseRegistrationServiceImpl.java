package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.AddRemarksRequestDto;
import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.AssignCounselorRequestDto;
import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.RegistrationDecisionRequestDto;
import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.RegistrationStatisticsDto;
import com.consultancy.education.DTOs.responseDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationResponseDto;
import com.consultancy.education.enums.ApprovalStatus;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.StudentCollegeCourseRegistration;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.StudentCollegeCourseRegistrationRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.service.AdminStudentCollegeCourseRegistrationService;
import com.consultancy.education.transformer.StudentCollegeCourseRegistrationTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudentCollegeCourseRegistrationServiceImpl implements AdminStudentCollegeCourseRegistrationService {

    private final StudentCollegeCourseRegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    @Override
    public List<StudentCollegeCourseRegistrationResponseDto> listRegistrations(
            String status,
            Long studentId,
            Long collegeId,
            Long courseId,
            Integer intakeYear,
            String dateFrom,
            String dateTo) {
        log.info(
                "Service: Fetching registrations with filters - status={}, studentId={}, collegeId={}, courseId={}, intakeYear={}, dateFrom={}, dateTo={}",
                status, studentId, collegeId, courseId, intakeYear, dateFrom, dateTo);

        List<StudentCollegeCourseRegistration> registrations = registrationRepository.findAll();

        Stream<StudentCollegeCourseRegistration> stream = registrations.stream();

        if (status != null) {
            stream = stream.filter(r -> r.getApplicationStatus().name().equalsIgnoreCase(status));
        }
        if (studentId != null) {
            stream = stream.filter(r -> r.getStudent().getId().equals(studentId));
        }
        if (collegeId != null) {
            stream = stream.filter(r -> r.getCollegeCourseSnapshot().getCollegeId().equals(collegeId));
        }
        if (courseId != null) {
            stream = stream.filter(r -> r.getCollegeCourseSnapshot().getCourseId().equals(courseId));
        }
        if (intakeYear != null) {
            stream = stream.filter(r -> r.getApplicationYear().equals(intakeYear));
        }
        // Optionally parse dateFrom/dateTo and filter by createdAt

        List<StudentCollegeCourseRegistrationResponseDto> result = stream
                .map(StudentCollegeCourseRegistrationTransformer::toResDto)
                .collect(Collectors.toList());

        log.info("Service: {} registrations matched filters", result.size());
        return result;
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto getRegistrationById(Long registrationId) {
        log.info("Admin Service: Fetching registration by id={}", registrationId);

        StudentCollegeCourseRegistration reg = registrationRepository.findById(registrationId)
                .orElseThrow(() -> {
                    log.warn("Admin Service: Registration not found, id={}", registrationId);
                    return new NotFoundException("Registration not found");
                });

        log.info("Admin Service: Registration found, id={}", registrationId);

        return StudentCollegeCourseRegistrationTransformer.toResDto(reg);
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto assignCounselor(AssignCounselorRequestDto requestDto) {
        log.info("Service: Assigning counselorId={} to registrationId={}", requestDto.getCounselorId(),
                requestDto.getRegistrationId());

        StudentCollegeCourseRegistration registration = registrationRepository.findById(requestDto.getRegistrationId())
                .orElseThrow(() -> {
                    log.warn("Service: Registration not found, id={}", requestDto.getRegistrationId());
                    return new NotFoundException("Registration not found");
                });

        User counselor = userRepository.findById(requestDto.getCounselorId())
                .orElseThrow(() -> {
                    log.warn("Service: User not found, id={}", requestDto.getCounselorId());
                    return new NotFoundException("User (counselor) not found");
                });

        // Optional: check that user has the COUNSELOR role
        if (counselor.getRole() == null || !"COUNSELOR".equalsIgnoreCase(counselor.getRole().getName())) {
            log.warn("Service: User id={} is not a counselor", requestDto.getCounselorId());
            throw new IllegalArgumentException("Selected user is not a counselor");
        }

        registration.setAssignedCounselor(counselor);
        registration.setUpdatedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Service: Counselor assigned successfully, registrationId={}", registration.getId());
        return StudentCollegeCourseRegistrationTransformer.toResDto(registration);
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto decideOnRegistration(RegistrationDecisionRequestDto requestDto) {
        log.info("Admin Service: Deciding on registrationId={} with decision={}", requestDto.getRegistrationId(),
                requestDto.getDecision());

        StudentCollegeCourseRegistration registration = registrationRepository.findById(requestDto.getRegistrationId())
                .orElseThrow(() -> {
                    log.warn("Admin Service: Registration not found, id={}", requestDto.getRegistrationId());
                    return new NotFoundException("Registration not found");
                });

        // Only allow action on SUBMITTED registrations
        if (!ApprovalStatus.SUBMITTED.equals(registration.getApplicationStatus())) {
            log.warn("Admin Service: Cannot decide on non-SUBMITTED registration, id={}, status={}",
                    registration.getId(), registration.getApplicationStatus());
            throw new IllegalStateException("Only SUBMITTED registrations can be approved or rejected");
        }

        // Decision must be "APPROVED" or "REJECTED"
        ApprovalStatus decisionStatus;
        try {
            decisionStatus = ApprovalStatus.valueOf(requestDto.getDecision().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Admin Service: Invalid decision value: {}", requestDto.getDecision());
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }

        if (!(decisionStatus == ApprovalStatus.APPROVED || decisionStatus == ApprovalStatus.REJECTED)) {
            log.warn("Admin Service: Decision not allowed: {}", decisionStatus);
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED");
        }

        registration.setApplicationStatus(decisionStatus);
        registration.setRemarks(requestDto.getRemarks());
        registration.setUpdatedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Admin Service: Registration id={} marked as {}", registration.getId(), decisionStatus);
        return StudentCollegeCourseRegistrationTransformer.toResDto(registration);
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto addRemarks(AddRemarksRequestDto requestDto) {
        log.info("Admin Service: Adding remarks to registrationId={}", requestDto.getRegistrationId());

        StudentCollegeCourseRegistration registration = registrationRepository.findById(requestDto.getRegistrationId())
                .orElseThrow(() -> {
                    log.warn("Admin Service: Registration not found, id={}", requestDto.getRegistrationId());
                    return new NotFoundException("Registration not found");
                });

        registration.setRemarks(requestDto.getRemarks());
        registration.setUpdatedAt(LocalDateTime.now());
        registrationRepository.save(registration);

        log.info("Admin Service: Remarks added successfully, registrationId={}", registration.getId());
        return StudentCollegeCourseRegistrationTransformer.toResDto(registration);
    }

    @Override
    public RegistrationStatisticsDto getStatistics() {
        log.info("Service: Calculating registration statistics");
        List<StudentCollegeCourseRegistration> allRegs = registrationRepository.findAll();

        long total = allRegs.size();
        long pending = allRegs.stream().filter(r -> r.getApplicationStatus().name().equals("PENDING")).count();
        long submitted = allRegs.stream().filter(r -> r.getApplicationStatus().name().equals("SUBMITTED")).count();
        long approved = allRegs.stream().filter(r -> r.getApplicationStatus().name().equals("APPROVED")).count();
        long rejected = allRegs.stream().filter(r -> r.getApplicationStatus().name().equals("REJECTED")).count();

        // Group by College
        Map<Long, List<StudentCollegeCourseRegistration>> byCollege = allRegs.stream()
                .collect(Collectors.groupingBy(r -> r.getCollegeCourseSnapshot().getCollegeId()));

        List<RegistrationStatisticsDto.StatusCount> collegeCounts = byCollege.entrySet().stream()
                .map(e -> RegistrationStatisticsDto.StatusCount.builder()
                        .id(e.getKey())
                        .name(e.getValue().get(0).getCollegeCourseSnapshot().getCollegeName())
                        .count(e.getValue().size())
                        .build())
                .collect(Collectors.toList());

        // Group by Course
        Map<Long, List<StudentCollegeCourseRegistration>> byCourse = allRegs.stream()
                .collect(Collectors.groupingBy(r -> r.getCollegeCourseSnapshot().getCourseId()));

        List<RegistrationStatisticsDto.StatusCount> courseCounts = byCourse.entrySet().stream()
                .map(e -> RegistrationStatisticsDto.StatusCount.builder()
                        .id(e.getKey())
                        .name(e.getValue().get(0).getCollegeCourseSnapshot().getCourseName())
                        .count(e.getValue().size())
                        .build())
                .collect(Collectors.toList());

        // Group by Intake Year
        Map<Integer, List<StudentCollegeCourseRegistration>> byYear = allRegs.stream()
                .collect(Collectors.groupingBy(StudentCollegeCourseRegistration::getApplicationYear));

        List<RegistrationStatisticsDto.YearCount> yearCounts = byYear.entrySet().stream()
                .map(e -> RegistrationStatisticsDto.YearCount.builder()
                        .year(e.getKey())
                        .count(e.getValue().size())
                        .build())
                .collect(Collectors.toList());

        RegistrationStatisticsDto stats = RegistrationStatisticsDto.builder()
                .totalRegistrations(total)
                .pending(pending)
                .submitted(submitted)
                .approved(approved)
                .rejected(rejected)
                .byCollege(collegeCounts)
                .byCourse(courseCounts)
                .byIntakeYear(yearCounts)
                .build();

        log.info("Service: Statistics calculation completed");
        return stats;
    }

}