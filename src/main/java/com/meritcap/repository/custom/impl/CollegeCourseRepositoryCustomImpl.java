package com.meritcap.repository.custom.impl;

import com.meritcap.DTOs.requestDTOs.search.SearchCourseRequestDto;
import com.meritcap.DTOs.responseDTOs.collegeCourse.SearchCollegeCourseResponseDto;
import com.meritcap.DTOs.responseDTOs.search.SearchCourseResponseDto;
import com.meritcap.repository.custom.CollegeCourseRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

public class CollegeCourseRepositoryCustomImpl implements CollegeCourseRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public SearchCourseResponseDto<SearchCollegeCourseResponseDto> searchCollegeCourses(SearchCourseRequestDto searchCourseRequestDto) {
        // Get the total rows count
        long totalRows = getCountQuery(searchCourseRequestDto);

        // Calculate the total pages
        int limitPerPage = searchCourseRequestDto.getPagination().getSize();
        int totalPages = (int) Math.ceil((double) totalRows / limitPerPage);

        // Get the paginated query results
        List<SearchCollegeCourseResponseDto> result = getPaginatedQuery(searchCourseRequestDto);

        // Create a Pagination object
        SearchCourseResponseDto.Pagination pagination = new SearchCourseResponseDto.Pagination(
                searchCourseRequestDto.getPagination().getPage(),
                limitPerPage,
                totalPages,
                totalRows
        );

        // Create the final response object
        SearchCourseResponseDto<SearchCollegeCourseResponseDto> response = new SearchCourseResponseDto<>();
        response.setData(result);
        response.setPagination(pagination);

        return response;
    }

    private long getCountQuery(SearchCourseRequestDto searchCourseRequestDto) {
        // Build the count query dynamically based on the filters
            StringBuilder countQueryStr = new StringBuilder("SELECT COUNT(DISTINCT cc.id) " +
                "FROM college_courses cc " +
                "INNER JOIN colleges clg ON clg.id = cc.college_id " +
                "INNER JOIN courses crs ON crs.id = cc.course_id WHERE 1=1 ");

        // Add conditions for the count query
        addFilterConditions(countQueryStr, searchCourseRequestDto);

        // Execute the count query
        Query countQuery = entityManager.createNativeQuery(countQueryStr.toString());

        // Set parameters dynamically for the count query
        setQueryParameters(countQuery, searchCourseRequestDto);

        // Get the total row count
        return ((Number) countQuery.getSingleResult()).longValue();
    }

    private List<SearchCollegeCourseResponseDto> getPaginatedQuery(SearchCourseRequestDto searchCourseRequestDto) {
        // Build the query dynamically for the paginated results
        StringBuilder queryStr = new StringBuilder("SELECT DISTINCT " +
                "cc.id AS collegeCourseId, " +
                "cc.college_id AS collegeId, " +
                "clg.name AS collegeName, " +
                "cc.course_id AS courseId, " +
                "crs.name AS courseName, " +
                "clg.campus_code AS campusCode, " +
                "clg.campus_name AS campusName, " +
                "clg.country AS country, " +
                "clg.country_id AS countryId, " +
                "crs.graduation_level AS graduationLevel, " +
                "clg.college_logo AS collegeImage, " +
                "cc.intake_year AS intakeYear, " +
                "cc.tuition_fee AS tuitionFee, " +
                "clg.established_year AS establishedYear, " +
                "GROUP_CONCAT(cim.intake_months ORDER BY cim.intake_months) AS intakeMonths " +
                "FROM college_courses cc " +
                "INNER JOIN colleges clg ON clg.id = cc.college_id " +
                "INNER JOIN courses crs ON crs.id = cc.course_id " +
                "LEFT JOIN course_intake_months cim ON cim.college_course_id = cc.id " +
                "WHERE 1=1 ");


        // Add conditions for the main query
        addFilterConditions(queryStr, searchCourseRequestDto);

        // ➡️ Add group by after filters
        queryStr.append(" GROUP BY cc.id, clg.name, crs.name, clg.campus_code, clg.campus_name, clg.country, clg.country_id, " +
                "crs.graduation_level, clg.college_logo, cc.intake_year, cc.tuition_fee, clg.established_year ");

        // Add pagination if available
        if (searchCourseRequestDto.getPagination() != null) {
            int size = searchCourseRequestDto.getPagination().getSize();
            int offset = (searchCourseRequestDto.getPagination().getPage() - 1) * size;
            queryStr.append(" LIMIT ").append(size).append(" OFFSET ").append(offset);
        }

        // Execute the query and set parameters dynamically
        Query query = entityManager.createNativeQuery(queryStr.toString(), SearchCollegeCourseResponseDto.class);

        // Set parameters dynamically for the main query
        setQueryParameters(query, searchCourseRequestDto);

        // Get the paginated result list
        return query.getResultList();
    }

    private void addFilterConditions(StringBuilder queryStr, SearchCourseRequestDto searchCourseRequestDto) {

        if (searchCourseRequestDto.getFilters().getCourses() != null && !searchCourseRequestDto.getFilters().getCourses().isEmpty()) {
            queryStr.append("AND crs.name IN (:courses) ");
        }
        if (searchCourseRequestDto.getFilters().getDepartments() != null && !searchCourseRequestDto.getFilters().getDepartments().isEmpty()) {
            queryStr.append("AND crs.department IN (:departments) ");
        }
        if (searchCourseRequestDto.getFilters().getGraduationLevels() != null && !searchCourseRequestDto.getFilters().getGraduationLevels().isEmpty()) {
            queryStr.append("AND crs.graduation_level IN (:graduationLevels) ");
        }
        if (searchCourseRequestDto.getFilters().getCountries() != null && !searchCourseRequestDto.getFilters().getCountries().isEmpty()) {
            queryStr.append("AND clg.country IN (:countries) ");
        }

        // Duration filter (with checks)
        SearchCourseRequestDto.Filters.DurationFilter duration = searchCourseRequestDto.getFilters().getDuration();
        if (duration != null) {
            Integer min = duration.getMinMonths();
            Integer max = duration.getMaxMonths();
            if (min != null && min > 0) {
                queryStr.append("AND cc.duration >= :minDuration ");
            }
            if (max != null && max > 0) {
                queryStr.append("AND cc.duration <= :maxDuration ");
            }
        }

        // Intake month filter
        if (searchCourseRequestDto.getFilters().getIntakeMonths() != null && !searchCourseRequestDto.getFilters().getIntakeMonths().isEmpty()) {
            queryStr.append("AND EXISTS (SELECT 1 FROM course_intake_months cim WHERE cim.college_course_id = cc.id AND cim.intake_months IN (:intakeMonths)) ");
        }

        // Search term (case insensitive)
        if (searchCourseRequestDto.getSearch() != null && searchCourseRequestDto.getSearch().getTerm() != null) {
            queryStr.append("AND (LOWER(crs.name) LIKE LOWER(:searchTerm) OR LOWER(crs.department) LIKE LOWER(:searchTerm)) ");
        }
    }

    private void setQueryParameters(Query query, SearchCourseRequestDto searchCourseRequestDto) {
        if (searchCourseRequestDto.getFilters() != null) {
            if (searchCourseRequestDto.getFilters().getCourses() != null && !searchCourseRequestDto.getFilters().getCourses().isEmpty()) {
                query.setParameter("courses", searchCourseRequestDto.getFilters().getCourses());
            }
            if (searchCourseRequestDto.getFilters().getDepartments() != null && !searchCourseRequestDto.getFilters().getDepartments().isEmpty()) {
                query.setParameter("departments", searchCourseRequestDto.getFilters().getDepartments());
            }
            if (searchCourseRequestDto.getFilters().getGraduationLevels() != null && !searchCourseRequestDto.getFilters().getGraduationLevels().isEmpty()) {
                query.setParameter("graduationLevels", searchCourseRequestDto.getFilters().getGraduationLevels());
            }
            if (searchCourseRequestDto.getFilters().getCountries() != null && !searchCourseRequestDto.getFilters().getCountries().isEmpty()) {
                query.setParameter("countries", searchCourseRequestDto.getFilters().getCountries());
            }

            if (searchCourseRequestDto.getFilters().getDuration() != null) {
                SearchCourseRequestDto.Filters.DurationFilter duration = searchCourseRequestDto.getFilters().getDuration();
                if (duration.getMinMonths() > 0) {
                    query.setParameter("minDuration", duration.getMinMonths());
                }
                if (duration.getMaxMonths() > 0) {
                    query.setParameter("maxDuration", duration.getMaxMonths());
                }
            }
            if (searchCourseRequestDto.getFilters().getIntakeMonths() != null && !searchCourseRequestDto.getFilters().getIntakeMonths().isEmpty()) {
                query.setParameter("intakeMonths", searchCourseRequestDto.getFilters().getIntakeMonths());
            }
        }

        if (searchCourseRequestDto.getSearch() != null && searchCourseRequestDto.getSearch().getTerm() != null) {
            query.setParameter("searchTerm", "%" + searchCourseRequestDto.getSearch().getTerm() + "%");
        }
    }
}
