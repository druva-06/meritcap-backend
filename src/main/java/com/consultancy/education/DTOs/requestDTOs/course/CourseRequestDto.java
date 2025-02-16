package com.consultancy.education.DTOs.requestDTOs.course;

import com.consultancy.education.enums.GraduationLevel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CourseRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Department is required")
    private String department;

    @NotNull(message = "Graduation level is required")
    @Enumerated(EnumType.STRING)
    private GraduationLevel graduationLevel;

    private String specialization;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseRequestDto that = (CourseRequestDto) obj;
        return Objects.equals(name, that.name) && Objects.equals(department, that.department) && graduationLevel.equals(that.graduationLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, department, graduationLevel);
    }

}
