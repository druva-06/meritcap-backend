package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.studentEducation.StudentEducationRequestDto;
import com.consultancy.education.DTOs.responseDTOs.studentEducation.StudentEducationResponseDto;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.StudentEducation;
import com.consultancy.education.repository.StudentEducationRepository;
import com.consultancy.education.repository.StudentRepository;
import com.consultancy.education.service.StudentEducationService;
import com.consultancy.education.transformer.StudentEducationTransformer;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentEducationServiceImpl implements StudentEducationService {

    private final StudentEducationRepository studentEducationRepository;
    private final StudentRepository studentRepository;

    public StudentEducationServiceImpl(StudentEducationRepository studentEducationRepository, StudentRepository studentRepository) {
        this.studentEducationRepository = studentEducationRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public StudentEducationResponseDto addStudentEducation(StudentEducationRequestDto studentEducationRequestDto, Long userId) {
        Student student = studentRepository.findByUserId(userId);
        if(student != null){
            StudentEducation studentEducation = StudentEducationTransformer.toEntity(studentEducationRequestDto);
            studentEducation.setStudent(student);
            student.getStudentEducations().add(studentEducation);
            studentEducation = studentEducationRepository.save(studentEducation);
            return StudentEducationTransformer.toResDTO(studentEducation, student);
        }
        throw new NotFoundException("Student not found");
    }

    @Override
    public StudentEducationResponseDto updateStudentEducation(StudentEducationRequestDto studentEducationRequestDto, Long studentEducationId){
        if(studentEducationRepository.findById(studentEducationId).isPresent()){
            StudentEducation studentEducation = studentEducationRepository.findById(studentEducationId).get();
            Student student = studentEducation.getStudent();
            StudentEducationTransformer.updateStudentEducation(studentEducation, studentEducationRequestDto);
            studentEducationRepository.save(studentEducation);
            return StudentEducationTransformer.toResDTO(studentEducation, student);
        }
        throw new NotFoundException("Student education not found");
    }

    @Override
    public List<StudentEducationResponseDto> getStudentEducation(Long userId) {
        Student student = studentRepository.findByUserId(userId);
        if(student != null){
            List<StudentEducation> studentEducations = student.getStudentEducations();
            return StudentEducationTransformer.toResDTO(studentEducations, student);
        }
        throw new NotFoundException("Student not found");
    }
}
