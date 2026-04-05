package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.meritcap.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Document;
import com.meritcap.model.Student;
import com.meritcap.model.StudentEducation;
import com.meritcap.repository.DocumentRepository;
import com.meritcap.repository.StudentEducationRepository;
import com.meritcap.repository.StudentRepository;
import com.meritcap.service.StudentEducationService;
import com.meritcap.transformer.StudentEducationTransformer;
import com.meritcap.transformer.DocumentTransformer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class StudentEducationServiceImpl implements StudentEducationService {

    private final StudentEducationRepository studentEducationRepository;
    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;

    public StudentEducationServiceImpl(StudentEducationRepository studentEducationRepository,
                                       StudentRepository studentRepository,
                                       DocumentRepository documentRepository) {
        this.studentEducationRepository = studentEducationRepository;
        this.studentRepository = studentRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    @Transactional
    public StudentEducationResponseDto addStudentEducation(StudentEducationRequestDto dto, Long userId) {
        log.info("Adding student education for user: {}", userId);

        Student student = studentRepository.findByUserId(userId);
        if (student == null) {
            log.error("Student not found for userId: {}", userId);
            throw new NotFoundException("Student not found");
        }

        boolean exists = studentEducationRepository.existsByStudentIdAndEducationLevelAndInstitutionName(
                student.getId(), dto.getEducationLevel(), dto.getInstitutionName());

        if (exists) {
            log.warn("Duplicate education detected for userId: {}", userId);
            throw new AlreadyExistException(List.of("Duplicate education record exists"));
        }

        StudentEducation education = StudentEducationTransformer.toEntity(dto);
        education.setStudent(student);
        StudentEducation saved = studentEducationRepository.save(education);

        log.info("Student education added: {}", saved.getId());
        return StudentEducationTransformer.toResDTO(saved, null);
    }

    @Override
    @Transactional
    public StudentEducationResponseDto updateStudentEducation(StudentEducationRequestDto dto, Long educationId) {
        log.info("Updating student education: {}", educationId);

        StudentEducation education = studentEducationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("Education record not found"));

        StudentEducationTransformer.updateStudentEducation(education, dto);
        studentEducationRepository.save(education);

        log.info("Student education updated: {}", educationId);
        return StudentEducationTransformer.toResDTO(education,
                education.getCertificateDocument() != null
                        ? DocumentTransformer.toDto(education.getCertificateDocument())
                        : null
        );
    }

    @Override
    @Transactional
    public StudentEducationResponseDto updateStudentEducationForCurrentUser(StudentEducationRequestDto dto, Long educationId, Long currentUserId) {
        StudentEducation education = studentEducationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("Education record not found"));
        if (education.getStudent() == null || education.getStudent().getUser() == null
                || !education.getStudent().getUser().getId().equals(currentUserId)) {
            throw new CustomException("You do not have permission to access this resource");
        }
        return updateStudentEducation(dto, educationId);
    }

    @Override
    @Transactional
    public void deleteStudentEducation(Long educationId) {
        log.info("Deleting student education: {}", educationId);

        StudentEducation education = studentEducationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("Education record not found"));
        // Optionally delete the linked certificate document
        if (education.getCertificateDocument() != null) {
            documentRepository.delete(education.getCertificateDocument());
            log.info("Deleted associated certificate document: {}", education.getCertificateDocument().getId());
        }
        studentEducationRepository.delete(education);
        log.info("Student education deleted: {}", educationId);
    }

    @Override
    @Transactional
    public void deleteStudentEducationForCurrentUser(Long educationId, Long currentUserId) {
        StudentEducation education = studentEducationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("Education record not found"));
        if (education.getStudent() == null || education.getStudent().getUser() == null
                || !education.getStudent().getUser().getId().equals(currentUserId)) {
            throw new CustomException("You do not have permission to access this resource");
        }
        deleteStudentEducation(educationId);
    }

    @Override
    public List<StudentEducationResponseDto> getStudentEducation(Long userId) {
        log.info("Fetching education records for user: {}", userId);

        Student student = studentRepository.findByUserId(userId);
        if (student == null) {
            log.error("Student not found for userId: {}", userId);
            throw new NotFoundException("Student not found");
        }
        List<StudentEducation> list = studentEducationRepository.findByStudentId(student.getId());
        return list.stream()
                .map(e -> StudentEducationTransformer.toResDTO(e,
                        e.getCertificateDocument() != null
                                ? DocumentTransformer.toDto(e.getCertificateDocument())
                                : null))
                .toList();
    }

    @Override
    @Transactional
    public void attachCertificate(Long educationId, Long documentId) {
        log.info("Attaching certificate (documentId={}) to educationId={}", documentId, educationId);

        StudentEducation education = studentEducationRepository.findById(educationId)
                .orElseThrow(() -> new NotFoundException("Education record not found"));
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        // Always replace
        education.setCertificateDocument(document);
        studentEducationRepository.save(education);

        log.info("Certificate attached to educationId={}", educationId);
    }
}
