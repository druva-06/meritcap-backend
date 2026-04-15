package com.meritcap.service.impl;

import com.meritcap.DTOs.responseDTOs.student.StudentSummaryResponseDto;
import com.meritcap.enums.ApprovalStatus;
import com.meritcap.enums.DocumentStatus;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Student;
import com.meritcap.model.StudentCollegeCourseRegistration;
import com.meritcap.model.Wishlist;
import com.meritcap.repository.DocumentRepository;
import com.meritcap.repository.StudentCollegeCourseRegistrationRepository;
import com.meritcap.repository.StudentEducationRepository;
import com.meritcap.repository.StudentRepository;
import com.meritcap.repository.WishlistRepository;
import com.meritcap.service.AdminStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStudentServiceImpl implements AdminStudentService {

    private final StudentRepository studentRepository;
    private final StudentCollegeCourseRegistrationRepository registrationRepository;
    private final DocumentRepository documentRepository;
    private final WishlistRepository wishlistRepository;
    private final StudentEducationRepository educationRepository;

    @Override
    public StudentSummaryResponseDto getStudentSummary(Long studentId) {
        log.info("Admin: Fetching summary for studentId={}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with id: " + studentId));

        // Application analytics
        List<StudentCollegeCourseRegistration> registrations =
                registrationRepository.findAllByStudentId(studentId);

        long totalApplications = registrations.size();
        long pendingApplications = registrations.stream()
                .filter(r -> ApprovalStatus.PENDING.equals(r.getApplicationStatus())).count();
        long submittedApplications = registrations.stream()
                .filter(r -> ApprovalStatus.SUBMITTED.equals(r.getApplicationStatus())).count();
        long approvedApplications = registrations.stream()
                .filter(r -> ApprovalStatus.APPROVED.equals(r.getApplicationStatus())).count();
        long rejectedApplications = registrations.stream()
                .filter(r -> ApprovalStatus.REJECTED.equals(r.getApplicationStatus())).count();

        // Document analytics
        var documents = documentRepository.findAllByReferenceTypeAndReferenceIdAndIsDeletedFalse("STUDENT", studentId);
        long totalDocuments = documents.size();
        long pendingDocuments = documents.stream()
                .filter(d -> DocumentStatus.PENDING.equals(d.getDocumentStatus())).count();
        long verifiedDocuments = documents.stream()
                .filter(d -> DocumentStatus.VERIFIED.equals(d.getDocumentStatus())).count();

        // Wishlist count
        Optional<Wishlist> wishlist = wishlistRepository.findByStudentId(studentId);
        long wishlistCount = wishlist.map(w -> w.getWishlistItems() != null ? (long) w.getWishlistItems().size() : 0L).orElse(0L);

        // Education records
        long educationRecordsCount = educationRepository.findByStudentId(studentId).size();

        // Build response
        var user = student.getUser();
        return StudentSummaryResponseDto.builder()
                .userId(user != null ? user.getId() : null)
                .studentId(student.getId())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .email(user != null ? user.getEmail() : null)
                .phoneNumber(user != null ? user.getPhoneNumber() : null)
                .profilePicture(user != null ? user.getProfilePicture() : null)
                .gender(student.getGender() != null ? student.getGender().toString() : null)
                .dateOfBirth(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : null)
                .graduationLevel(student.getGraduationLevel() != null ? student.getGraduationLevel().toString() : null)
                .profileActiveStatus(student.getProfileActiveStatus() != null ? student.getProfileActiveStatus().toString() : null)
                .profileCompletion(student.getProfileCompletion())
                .totalApplications(totalApplications)
                .pendingApplications(pendingApplications)
                .submittedApplications(submittedApplications)
                .approvedApplications(approvedApplications)
                .rejectedApplications(rejectedApplications)
                .totalDocuments(totalDocuments)
                .pendingDocuments(pendingDocuments)
                .verifiedDocuments(verifiedDocuments)
                .wishlistCount(wishlistCount)
                .educationRecordsCount(educationRecordsCount)
                .build();
    }
}
