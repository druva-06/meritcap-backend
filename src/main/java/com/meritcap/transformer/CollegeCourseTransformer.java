package com.meritcap.transformer;

import com.meritcap.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.meritcap.DTOs.responseDTOs.college.CollegeResponseDto;
import com.meritcap.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.meritcap.DTOs.responseDTOs.course.CourseResponseDto;
import com.meritcap.model.CollegeCourse;
import com.meritcap.utils.FormatConverter;

public class CollegeCourseTransformer {

    public static CollegeCourse excelToEntity(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto) {
        return CollegeCourse.builder()
                .courseUrl(collegeCourseRequestExcelDto.getCourseUrl())
                .duration(FormatConverter.cnvrtDurationToInteger(collegeCourseRequestExcelDto.getDuration())) // Need to
                                                                                                              // Change
                .intakeMonths(FormatConverter.cnvrtIntakesToList(collegeCourseRequestExcelDto.getIntakeMonths())) // Need
                                                                                                                  // to
                                                                                                                  // Change
                .intakeYear(collegeCourseRequestExcelDto.getIntakeYear())
                .eligibilityCriteria(collegeCourseRequestExcelDto.getEligibilityCriteria())
                .applicationFee(collegeCourseRequestExcelDto.getApplicationFee())
                .tuitionFee(collegeCourseRequestExcelDto.getTuitionFee())
                .ieltsMinScore(collegeCourseRequestExcelDto.getIeltsMinScore())
                .ieltsMinBandScore(collegeCourseRequestExcelDto.getIeltsMinBandScore())
                .toeflMinScore(collegeCourseRequestExcelDto.getToeflMinScore())
                .toeflMinBandScore(collegeCourseRequestExcelDto.getToeflMinBandScore())
                .pteMinScore(collegeCourseRequestExcelDto.getPteMinScore())
                .pteMinBandScore(collegeCourseRequestExcelDto.getPteMinBandScore())
                .detMinScore(collegeCourseRequestExcelDto.getDetMinScore())
                .greMinScore(collegeCourseRequestExcelDto.getGreMinScore())
                .gmatMinScore(collegeCourseRequestExcelDto.getGmatMinScore())
                .satMinScore(collegeCourseRequestExcelDto.getSatMinScore())
                .catMinScore(collegeCourseRequestExcelDto.getCatMinScore())
                .min10thScore(collegeCourseRequestExcelDto.getMin10thScore())
                .minInterScore(collegeCourseRequestExcelDto.getMinInterScore())
                .minGraduationScore(collegeCourseRequestExcelDto.getMinGraduationScore())
                .scholarshipEligible(collegeCourseRequestExcelDto.getScholarshipEligible())
                .scholarshipDetails(collegeCourseRequestExcelDto.getScholarshipDetails())
                .backlogAcceptanceRange(collegeCourseRequestExcelDto.getBacklogAcceptanceRange())
                .remarks(collegeCourseRequestExcelDto.getRemarks())
                .credits(collegeCourseRequestExcelDto.getCredits())
                .detailedScholarshipInfo(collegeCourseRequestExcelDto.getDetailedScholarshipInfo())
                .whyChooseThisCourse(collegeCourseRequestExcelDto.getWhyChooseThisCourse())
                .aboutCourse(collegeCourseRequestExcelDto.getAboutCourse())
                .keyFeatures(collegeCourseRequestExcelDto.getKeyFeatures())
                .learningOutcomes(collegeCourseRequestExcelDto.getLearningOutcomes())
                .courseHighlights(collegeCourseRequestExcelDto.getCourseHighlights())
                .careerOpportunity(collegeCourseRequestExcelDto.getCareerOpportunity())
                .faqsCourse(collegeCourseRequestExcelDto.getFaqsCourse())
                .coreModules(collegeCourseRequestExcelDto.getCoreModules())
                .assessmentMethods(collegeCourseRequestExcelDto.getAssessmentMethods())
                .jobMarkets(collegeCourseRequestExcelDto.getJobMarkets())
                .build();
    }

    // public static CollegeCourse toEntity(CollegeCourseRequestExcelDto
    // collegeCourseRequestExcelDto) {
    // return CollegeCourse.builder()
    // .courseUrl(collegeCourseRequestExcelDto.getCourseUrl())
    // .duration(collegeCourseRequestExcelDto.getDuration())
    //
    // .build();
    // }
    //
    public static CollegeCourseResponseDto toResDto(CollegeCourse collegeCourse, CollegeResponseDto collegeResponseDto,
            CourseResponseDto courseResponseDto) {
        return CollegeCourseResponseDto.builder()
                .college(collegeResponseDto)
                .course(courseResponseDto)
                .collegeCourseId(collegeCourse.getId())
                .courseUrl(collegeCourse.getCourseUrl())
                .tuitionFee(collegeCourse.getTuitionFee())
                .applicationFee(collegeCourse.getApplicationFee())
                .duration(collegeCourse.getDuration())
                .backlogAcceptanceRange(collegeCourse.getBacklogAcceptanceRange())
                .eligibilityCriteria(collegeCourse.getEligibilityCriteria())
                .intakeYear(collegeCourse.getIntakeYear())
                .intakeMonths(collegeCourse.getIntakeMonths())
                .remarks(collegeCourse.getRemarks())
                .scholarshipEligible(collegeCourse.getScholarshipEligible())
                .scholarshipDetails(collegeCourse.getScholarshipDetails())
                .min10thScore(collegeCourse.getMin10thScore())
                .minInterScore(collegeCourse.getMinInterScore())
                .minGraduationScore(collegeCourse.getMinGraduationScore())
                .toeflMinScore(collegeCourse.getToeflMinScore())
                .toeflMinBandScore(collegeCourse.getToeflMinBandScore())
                .pteMinScore(collegeCourse.getPteMinScore())
                .pteMinBandScore(collegeCourse.getPteMinBandScore())
                .ieltsMinScore(collegeCourse.getIeltsMinScore())
                .ieltsMinBandScore(collegeCourse.getIeltsMinBandScore())
                .satMinScore(collegeCourse.getSatMinScore())
                .greMinScore(collegeCourse.getGreMinScore())
                .gmatMinScore(collegeCourse.getGmatMinScore())
                .detMinScore(collegeCourse.getDetMinScore())
                .catMinScore(collegeCourse.getCatMinScore())
                .credits(collegeCourse.getCredits())
                .detailedScholarshipInfo(collegeCourse.getDetailedScholarshipInfo())
                .whyChooseThisCourse(collegeCourse.getWhyChooseThisCourse())
                .aboutCourse(collegeCourse.getAboutCourse())
                .keyFeatures(collegeCourse.getKeyFeatures())
                .learningOutcomes(collegeCourse.getLearningOutcomes())
                .courseHighlights(collegeCourse.getCourseHighlights())
                .careerOpportunity(collegeCourse.getCareerOpportunity())
                .faqsCourse(collegeCourse.getFaqsCourse())
                .coreModules(collegeCourse.getCoreModules())
                .assessmentMethods(collegeCourse.getAssessmentMethods())
                .jobMarkets(collegeCourse.getJobMarkets())
                .build();
    }
    //
    // public static List<CollegeCourseResponseDto> toResDto(List<CollegeCourse>
    // collegeCourses) {
    // List<CollegeCourseResponseDto> collegeCourseResponseDtos = new ArrayList<>();
    // for (CollegeCourse collegeCourse : collegeCourses) {
    // collegeCourseResponseDtos.add(toResDto(collegeCourse, collegeCourse.getId(),
    // collegeCourse.getCollege().getName(), collegeCourse.getCourse().getName()));
    // }
    // return collegeCourseResponseDtos;
    // }
    //
    // public static void updateCollegeCourse(CollegeCourse collegeCourse,
    // CollegeCourseRequestExcelDto collegeCourseRequestExcelDto) {
    // //collegeCourse.setIntakeMonth(collegeCourseRequestDto.getIntakeMonth());
    // collegeCourse.setIntakeYear(collegeCourseRequestExcelDto.getIntakeYear());
    // collegeCourse.setTuitionFee(collegeCourseRequestExcelDto.getTuitionFee());
    // collegeCourse.setApplicationFee(collegeCourseRequestExcelDto.getApplicationFee());
    // collegeCourse.setDuration(collegeCourseRequestExcelDto.getDuration());
    // collegeCourse.setApplicationDeadline(collegeCourseRequestExcelDto.getApplicationDeadline());
    // collegeCourse.setMaxStudents(collegeCourseRequestExcelDto.getMaxStudents());
    // collegeCourse.setStatus(collegeCourseRequestExcelDto.getStatus());
    // }
}
