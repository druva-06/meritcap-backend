package com.consultancy.education.validations;
import com.consultancy.education.model.Course;

import java.util.Objects;

public class CourseValidations {
    public static Boolean validateCourseData(Course existingCourse, Course course){
        if (existingCourse != null && course != null) {
            return Objects.equals(existingCourse.getSpecialization(), course.getSpecialization());
        }

        return false;
    }
}
