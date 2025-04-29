package com.consultancy.education.repository;

import com.consultancy.education.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByUserId(Long id);

//    Student findByEmail(String email);
//
//    Student findByUsername(String username);
//
//    Student findByPhoneNumber(String phoneNumber);

//    Student findByAadhaarNumber(String aadhaarNumber);
//
//    Student findByPassportNumber(String passportNumber);
}
