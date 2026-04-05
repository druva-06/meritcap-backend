package com.meritcap.repository;

import com.meritcap.model.StudentCollegeCourseRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentCollegeCourseRegistrationRepository extends JpaRepository<StudentCollegeCourseRegistration, Long> {
    Optional<StudentCollegeCourseRegistration> findByStudentIdAndCollegeCourseSnapshot_CourseIdAndIntakeSessionAndApplicationYear(
            Long studentId, Long courseId, String intakeSession, Integer applicationYear
    );
    List<StudentCollegeCourseRegistration> findAllByStudentId(Long studentId);

    @Modifying
    @Query("UPDATE StudentCollegeCourseRegistration r SET r.assignedCounselor = null WHERE r.assignedCounselor.id = :userId")
    int clearAssignedCounselorReferences(@Param("userId") Long userId);
}
