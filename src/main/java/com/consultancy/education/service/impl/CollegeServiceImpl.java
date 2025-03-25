package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.exception.DatabaseException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.exception.ValidationException;
import com.consultancy.education.helper.ExcelHelper;
import com.consultancy.education.model.College;
import com.consultancy.education.repository.CollegeRepository;
import com.consultancy.education.service.CollegeService;
import com.consultancy.education.transformer.CollegeTransformer;
import com.consultancy.education.utils.PatternConvert;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CollegeServiceImpl implements CollegeService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CollegeRepository collegeRepository;

    public CollegeServiceImpl(CollegeRepository collegeRepository) {
        this.collegeRepository = collegeRepository;
    }

    private String escapeSqlString(String value) {
        return value != null ? value.replace("'", "''") : null;
    }

    @Override
    @Transactional
    public String bulkCollegesUpload(MultipartFile file) {
        try {
            // Convert Excel Data into Map
            Map<String, College> colleges = ExcelHelper.convertCollegeExcelIntoList(file.getInputStream());

            if (colleges.isEmpty()) {
                return "No colleges to upload";
            }

            String sql = """
                    INSERT INTO colleges (
                    campus_code, name, campus_name, website_url, college_logo, 
                    country, established_year, ranking, description, 
                    campus_gallery_video_link, created_at, updated_at
                ) VALUES %s
                ON DUPLICATE KEY UPDATE 
                    name = VALUES(name), 
                    campus_name = VALUES(campus_name), 
                    website_url = VALUES(website_url), 
                    college_logo = VALUES(college_logo), 
                    country = VALUES(country), 
                    established_year = VALUES(established_year), 
                    ranking = VALUES(ranking), 
                    description = VALUES(description), 
                    campus_gallery_video_link = VALUES(campus_gallery_video_link), 
                    updated_at = NOW()
            """;


            StringBuilder values = new StringBuilder();
            int batchSize = 100;
            int count = 0;
            List<String> batchQueries = new ArrayList<>();

            for (College college : colleges.values()) {
                values.append(String.format(
                        "('%s', '%s', '%s', %s, %s, '%s', %s, %s, %s, %s, NOW(), NOW()),",
                        escapeSqlString(college.getCampusCode()),
                        escapeSqlString(college.getName()),
                        escapeSqlString(college.getCampus()),
                        (college.getWebsiteUrl() != null ? "'" + escapeSqlString(college.getWebsiteUrl()) + "'" : "NULL"),
                        (college.getCollegeLogo() != null ? "'" + escapeSqlString(college.getCollegeLogo()) + "'" : "NULL"),
                        escapeSqlString(college.getCountry()),
                        (college.getEstablishedYear() != null ? college.getEstablishedYear() : "NULL"),
                        (college.getRanking() != null ? "'" + escapeSqlString(college.getRanking()) + "'" : "NULL"),
                        (college.getDescription() != null ? "'" + escapeSqlString(college.getDescription()) + "'" : "NULL"),
                        (college.getCampusGalleryVideoLink() != null ? "'" + escapeSqlString(college.getCampusGalleryVideoLink()) + "'" : "NULL")
                ));

                count++;
                if (count % batchSize == 0) {
                    batchQueries.add(String.format(sql, values.substring(0, values.length() - 1)));
                    values.setLength(0);
                }
            }

            if (!values.isEmpty()) {
                batchQueries.add(String.format(sql, values.substring(0, values.length() - 1)));
            }

            // Execute each batch separately
            for (String batchQuery : batchQueries) {
                entityManager.createNativeQuery(batchQuery).executeUpdate();
            }

            return "Colleges Uploaded Successfully!";

        } catch (Exception e) {
            throw new RuntimeException("Bulk Insert/Update failed!", e);
        }
    }

    @Override
    public CollegeResponseDto add(CollegeRequestDto collegeRequestDto) {
        try {
            College college = CollegeTransformer.toEntity(collegeRequestDto);
            collegeRepository.save(college);
            //return new CollegeResponseDto(college.getId(), college.getName(), college.getCampus(), college.getCampusCode());
            return null;
        }
        catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public List<CollegeResponseDto> getColleges() {
        try{
            List<College> collegeList = collegeRepository.findAll();
            return CollegeTransformer.toResDTO(collegeList);
        } catch (Exception e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public List<CollegeResponseDto> getCollegesByCountries(List<String> countries) {
        try{
            List<College> collegeList = new ArrayList<>(); // Update the code
            return CollegeTransformer.toResDTO(collegeList);
        }
        catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public CollegeRequestDto getCollege(Long id) {
        if (collegeRepository.findById(id).isPresent()) {
            College college = collegeRepository.findById(id).get();
            return CollegeTransformer.toReqDTO(college);
        } else {
            throw new NotFoundException("College not found");
        }
    }

    @Override
    public Long getCollegeCount() {
        try{
            return collegeRepository.count();
        }
        catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public CollegeResponseDto deleteCollege(Long id){
        if (collegeRepository.findById(id).isPresent()) {
            College college = collegeRepository.findById(id).get();
            collegeRepository.delete(college);
            return CollegeTransformer.toResDTO(college);
        }
        else {
            throw new NotFoundException("College not found");
        }
    }

    @Override
    public CollegeRequestDto updateCollege(Long id, CollegeRequestDto collegeRequestDto) {
        if(collegeRepository.findById(id).isPresent()){
            College college = collegeRepository.findById(id).get();
            CollegeTransformer.updateCollegeDetails(college, collegeRequestDto);
            return CollegeTransformer.toReqDTO(college);
        }
        else{
            throw new NotFoundException("College not found");
        }
    }

    @Override
    public List<CollegeResponseDto> getCollegeByName(String name) {
        List<College> colleges = collegeRepository.searchByNameOrCampus(PatternConvert.jumbleSearch(name), PageRequest.of(0, 5));
        if (!colleges.isEmpty()) {
            return CollegeTransformer.toResDTO(colleges);
        }
        else{
            throw new NotFoundException("College not found");
        }
    }

    @Override
    public List<CollegeResponseDto> sortCollegeByName(String type) {
        type = type.toUpperCase();
        List<College> colleges;
        if(type.equals("ASC")){
            colleges = collegeRepository.findAllByOrderByNameAsc();
        }
        else if(type.equals("DESC")){
            colleges = collegeRepository.findAllByOrderByNameDesc();
        }
        else{
            List<String> errors = new ArrayList<>();
            errors.add("Invalid sort type");
            throw new ValidationException(errors);
        }
        return CollegeTransformer.toResDTO(colleges);
    }

//    @Override
//    public List<CollegeCourseResponseDto> getCollegeCourses(Long collegeId) {
//        if(collegeRepository.findById(collegeId).isPresent()){
//            College college = collegeRepository.findById(collegeId).get();
//            List<CollegeCourse> collegeCourses = college.getCollegeCourses();
//            return CollegeCourseTransformer.toResDto(collegeCourses);
//        }
//        throw new NotFoundException("College not found");
//    }

    @Override
    public String updateInternalCollegeData() {
        List<College> collegeList = collegeRepository.findAll();
        collegeRepository.deleteAll();
        List<College> updatedCollegeList = new ArrayList<>();
        int count = 1;
        for (College college : collegeList) {
            List<String> campuses = List.of(college.getCampus().split(","));
            for(String campus : campuses) {
                College updatedCollege = CollegeTransformer.reqDtoToReqDto(college, campus.trim());
                if (Objects.equals(updatedCollege.getCountry(), "Australia")) {
                    updatedCollege.setCampusCode("AUS" + count);
                } else if (Objects.equals(updatedCollege.getCountry(), "Canada")) {
                    updatedCollege.setCampusCode("CAN" + count);
                } else if (Objects.equals(updatedCollege.getCountry(), "United States of America")) {
                    updatedCollege.setCampusCode("USA" + count);
                }
                count++;
                updatedCollegeList.add(updatedCollege);

            }
        }
        collegeRepository.saveAll(updatedCollegeList);
        return "Updated Successfully";
    }
}
