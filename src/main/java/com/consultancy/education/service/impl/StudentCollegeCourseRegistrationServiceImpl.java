package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationEditRequestDto;
import com.consultancy.education.DTOs.requestDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentCollegeCourseRegistration.StudentCollegeCourseRegistrationResponseDto;
import com.consultancy.education.enums.ApprovalStatus;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.CollegeCourse;
import com.consultancy.education.model.CollegeCourseSnapshot;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.StudentCollegeCourseRegistration;
import com.consultancy.education.repository.CollegeCourseRepository;
import com.consultancy.education.repository.CollegeCourseSnapshotRepository;
import com.consultancy.education.repository.StudentCollegeCourseRegistrationRepository;
import com.consultancy.education.repository.StudentRepository;
import com.consultancy.education.service.DocumentService;
import com.consultancy.education.service.StudentCollegeCourseRegistrationService;
import com.consultancy.education.transformer.StudentCollegeCourseRegistrationTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StudentCollegeCourseRegistrationServiceImpl implements StudentCollegeCourseRegistrationService {

    private final StudentCollegeCourseRegistrationRepository studentCollegeCourseRegistrationRepository;
    private final StudentRepository studentRepository;
    private final CollegeCourseRepository collegeCourseRepository;
    private final CollegeCourseSnapshotRepository collegeCourseSnapshotRepository;

    StudentCollegeCourseRegistrationServiceImpl(StudentCollegeCourseRegistrationRepository studentCollegeCourseRegistrationRepository,
                                                StudentRepository studentRepository,
                                                CollegeCourseRepository collegeCourseRepository, CollegeCourseSnapshotRepository collegeCourseSnapshotRepository) {
        this.studentCollegeCourseRegistrationRepository = studentCollegeCourseRegistrationRepository;
        this.studentRepository = studentRepository;
        this.collegeCourseRepository = collegeCourseRepository;
        this.collegeCourseSnapshotRepository = collegeCourseSnapshotRepository;
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto registerStudentForCourse(StudentCollegeCourseRegistrationRequestDto requestDto) {
        log.info("Received registration request: studentId={}, collegeCourseId={}, intakeSession={}",
                requestDto.getStudentId(), requestDto.getCollegeCourseId(), requestDto.getIntakeSession());

        int year = extractYearFromIntake(requestDto.getIntakeSession());

        // Check for duplicates
        studentCollegeCourseRegistrationRepository.findByStudentIdAndCollegeCourseSnapshot_CourseIdAndIntakeSessionAndApplicationYear(
                        requestDto.getStudentId(), requestDto.getCollegeCourseId(), requestDto.getIntakeSession(), year)
                .ifPresent(reg -> {
                    log.warn("Duplicate registration detected for studentId={}, courseId={}, intake={}, year={}",
                            requestDto.getStudentId(), requestDto.getCollegeCourseId(), requestDto.getIntakeSession(), year);
                    throw new AlreadyExistException(Collections.singletonList("Application already exists for this student/course/intake/year"));
                });

        // Fetch and snapshot course
        CollegeCourse course = collegeCourseRepository.findById(requestDto.getCollegeCourseId())
                .orElseThrow(() -> {
                    log.warn("College course not found: {}", requestDto.getCollegeCourseId());
                    return new NotFoundException("College course not found");
                });

        CollegeCourseSnapshot snapshot = createSnapshotFromCourse(course);
        collegeCourseSnapshotRepository.save(snapshot);
        log.info("Created college course snapshot with id={}", snapshot.getId());

        // Fetch student
        Student student = studentRepository.findById(requestDto.getStudentId())
                .orElseThrow(() -> {
                    log.warn("Student not found: {}", requestDto.getStudentId());
                    return new NotFoundException("Student not found");
                });

        // Create registration
        StudentCollegeCourseRegistration registration = StudentCollegeCourseRegistration.builder()
                .student(student)
                .collegeCourseSnapshot(snapshot)
                .applicationStatus(ApprovalStatus.SUBMITTED)
                .intakeSession(requestDto.getIntakeSession())
                .applicationYear(year)
                .remarks(requestDto.getRemarks())
                .build();

        studentCollegeCourseRegistrationRepository.save(registration);
        log.info("Saved new registration with id={}", registration.getId());

        // Build response
        StudentCollegeCourseRegistrationResponseDto response = StudentCollegeCourseRegistrationResponseDto.builder()
                .registrationId(registration.getId())
                .studentId(student.getId())
                .collegeCourseSnapshotId(snapshot.getId())
                .intakeSession(registration.getIntakeSession())
                .applicationYear(year)
                .status(registration.getApplicationStatus().name())
                .remarks(registration.getRemarks())
                .createdAt(registration.getCreatedAt())
                .updatedAt(registration.getUpdatedAt())
                .courseName(snapshot.getCourseName())
                .collegeName(snapshot.getCollegeName())
                .build();

        log.info("Returning registration response for registrationId={}", registration.getId());
        return response;
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto editRegistration(StudentCollegeCourseRegistrationEditRequestDto requestDto) {
        log.info("Edit registration request received: registrationId={}, intakeSession={}",
                requestDto.getRegistrationId(), requestDto.getIntakeSession());

        StudentCollegeCourseRegistration registration = studentCollegeCourseRegistrationRepository.findById(requestDto.getRegistrationId())
                .orElseThrow(() -> {
                    log.warn("Registration not found: {}", requestDto.getRegistrationId());
                    return new NotFoundException("Registration not found");
                });

        if (!ApprovalStatus.PENDING.equals(registration.getApplicationStatus())) {
            log.warn("Attempted edit on registration with non-PENDING status: registrationId={}, status={}",
                    registration.getId(), registration.getApplicationStatus());
            throw new IllegalStateException("Only PENDING registrations can be edited");
        }

        registration.setIntakeSession(requestDto.getIntakeSession());
        registration.setRemarks(requestDto.getRemarks());
        registration.setUpdatedAt(LocalDateTime.now());

        studentCollegeCourseRegistrationRepository.save(registration);
        log.info("Registration edited and saved: registrationId={}", registration.getId());

        // Use the transformer to convert entity to response DTO
        return StudentCollegeCourseRegistrationTransformer.toResDto(registration);
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto getRegistrationById(Long registrationId) {
        log.info("Service: Fetching registration by id={}", registrationId);
        StudentCollegeCourseRegistration reg = studentCollegeCourseRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> {
                    log.warn("Service: Registration not found, id={}", registrationId);
                    return new NotFoundException("Registration not found");
                });

        log.info("Service: Registration found, id={}", registrationId);

        return StudentCollegeCourseRegistrationTransformer.toResDto(reg);
    }

    @Override
    public List<StudentCollegeCourseRegistrationResponseDto> getRegistrationsByStudentId(Long studentId) {
        log.info("Service: Fetching all registrations for studentId={}", studentId);
        List<StudentCollegeCourseRegistration> regs = studentCollegeCourseRegistrationRepository.findAllByStudentId(studentId);

        log.info("Service: Found {} registrations for studentId={}", regs.size(), studentId);

        return regs.stream()
                .map(StudentCollegeCourseRegistrationTransformer::toResDto)
                .collect(Collectors.toList());
    }

    @Override
    public StudentCollegeCourseRegistrationResponseDto submitRegistration(Long registrationId) {
        log.info("Service: Submitting registration id={}", registrationId);

        StudentCollegeCourseRegistration reg = studentCollegeCourseRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> {
                    log.warn("Service: Registration not found, id={}", registrationId);
                    return new NotFoundException("Registration not found");
                });

        if (!ApprovalStatus.PENDING.equals(reg.getApplicationStatus())) {
            log.warn("Service: Registration cannot be submitted in status={}", reg.getApplicationStatus());
            throw new IllegalStateException("Only PENDING registrations can be submitted");
        }

        // Check for required documents
//        List<String> missingDocs = documentService.getMissingDocumentsForRegistration(registrationId);
//        if (!missingDocs.isEmpty()) {
//            log.warn("Service: Registration submission blocked due to missing documents: {}", missingDocs);
//            throw new IllegalStateException("Cannot submit: missing documents: " + missingDocs);
//        }

        reg.setApplicationStatus(ApprovalStatus.SUBMITTED);
        reg.setUpdatedAt(LocalDateTime.now());
        studentCollegeCourseRegistrationRepository.save(reg);

        log.info("Service: Registration id={} submitted successfully", reg.getId());

        return StudentCollegeCourseRegistrationTransformer.toResDto(reg);
    }

    private int extractYearFromIntake(String intakeSession) {
        if (intakeSession == null) throw new IllegalArgumentException("Intake session is null");
        String[] parts = intakeSession.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d{4}")) {
                return Integer.parseInt(part);
            }
        }
        log.warn("No year found in intake session: {}", intakeSession);
        throw new IllegalArgumentException("No year found in intake session: " + intakeSession);
    }

    private CollegeCourseSnapshot createSnapshotFromCourse(CollegeCourse course) {
        log.info("Creating snapshot for courseId={}", course.getId());
        return CollegeCourseSnapshot.builder()
                .courseId(course.getCourse().getId())
                .courseName(course.getCourse().getName())
                .collegeId(course.getCollege().getId())
                .collegeName(course.getCollege().getName())
                .courseUrl(course.getCourseUrl())
                .duration(course.getDuration())
                .intakeMonths(
                        course.getIntakeMonths() != null ?
                                course.getIntakeMonths().stream().map(Enum::name).collect(Collectors.joining(",")) :
                                null
                )
                .intakeYear(course.getIntakeYear())
                .eligibilityCriteria(course.getEligibilityCriteria())
                .applicationFee(course.getApplicationFee())
                .tuitionFee(course.getTuitionFee())
                .ieltsMinScore(course.getIeltsMinScore())
                .ieltsMinBandScore(course.getIeltsMinBandScore())
                .toeflMinScore(course.getToeflMinScore())
                .toeflMinBandScore(course.getToeflMinBandScore())
                .pteMinScore(course.getPteMinScore())
                .pteMinBandScore(course.getPteMinBandScore())
                .detMinScore(course.getDetMinScore())
                .greMinScore(course.getGreMinScore())
                .gmatMinScore(course.getGmatMinScore())
                .satMinScore(course.getSatMinScore())
                .catMinScore(course.getCatMinScore())
                .min10thScore(course.getMin10thScore())
                .minInterScore(course.getMinInterScore())
                .minGraduationScore(course.getMinGraduationScore())
                .scholarshipEligible(course.getScholarshipEligible())
                .scholarshipDetails(course.getScholarshipDetails())
                .backlogAcceptanceRange(course.getBacklogAcceptanceRange())
                .remarks(course.getRemarks())
                .snapshotTakenAt(LocalDateTime.now())
                .build();
    }
}
