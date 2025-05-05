package com.consultancy.education.repository;

import com.consultancy.education.enums.GraduationLevel;
import com.consultancy.education.model.Course;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @Query("SELECT c FROM Course c WHERE c.name = :name AND c.department = :department AND c.graduationLevel = :graduationLevel AND c.specialization = :specialization")
    Course findDuplicate(@Param("name") String name, @Param("department") String department, @Param("graduationLevel") GraduationLevel graduationLevel, @Param("specialization") String specialization);

    List<Course> findByNameContainingIgnoreCase(String name, PageRequest of);

    @Query("SELECT e FROM Course e WHERE LOWER(e.name) LIKE LOWER(:input)")
    List<Course> searchByNameOrDepartment(@Param("input") String input, PageRequest of);

    Course findByNameAndDepartmentAndGraduationLevel(String name, String department, GraduationLevel graduationLevel);

    List<Course> findByNameAndGraduationLevel(String name, GraduationLevel graduationLevel);

    @Query("SELECT c FROM Course c WHERE CONCAT(c.name, '|', c.department, '|', c.graduationLevel) IN :courseKeys")
    List<Course> findByCourseKeys(@Param("courseKeys") Set<String> courseKeys);
}
