package com.meritcap.repository;

import com.meritcap.model.College;
import com.meritcap.model.CollegeCourse;
import com.meritcap.model.Course;
import com.meritcap.repository.custom.CollegeCourseRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollegeCourseRepository extends JpaRepository<CollegeCourse, Long>, CollegeCourseRepositoryCustom {

    boolean existsByCollegeAndCourse(College college, Course course);

    boolean existsByCollegeIdAndCourseId(Long collegeId, Long courseId);

    Optional<CollegeCourse> findByCollegeIdAndCourseId(Long collegeId, Long courseId);

    /**
     * Bulk-load all CollegeCourse rows for the given college IDs.
     * Used by bulk-upload to avoid N+1 queries per row.
     */
    @Query("SELECT cc FROM CollegeCourse cc WHERE cc.college.id IN :collegeIds")
    List<CollegeCourse> findByCollegeIdIn(@Param("collegeIds") Collection<Long> collegeIds);

}
