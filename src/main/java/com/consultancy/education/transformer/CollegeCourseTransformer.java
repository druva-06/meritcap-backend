package com.consultancy.education.transformer;


import com.consultancy.education.DTOs.requestDTOs.collegeCourse.CollegeCourseRequestExcelDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.DTOs.responseDTOs.collegeCourse.CollegeCourseResponseDto;
import com.consultancy.education.DTOs.responseDTOs.course.CourseResponseDto;
import com.consultancy.education.model.CollegeCourse;
import com.consultancy.education.utils.FormatConverter;

public class CollegeCourseTransformer {

    public static CollegeCourse excelToEntity(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto) {
        return CollegeCourse.builder()
                .courseUrl(collegeCourseRequestExcelDto.getCourseUrl())
                .duration(FormatConverter.cnvrtDurationToInteger(collegeCourseRequestExcelDto.getDuration())) // Need to Change
                .intakeMonths(FormatConverter.cnvrtIntakesToList(collegeCourseRequestExcelDto.getIntakeMonths())) // Need to Change
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
                .build();
    }

//    public static CollegeCourse toEntity(CollegeCourseRequestExcelDto collegeCourseRequestExcelDto) {
//        return CollegeCourse.builder()
//                .courseUrl(collegeCourseRequestExcelDto.getCourseUrl())
//                .duration(collegeCourseRequestExcelDto.getDuration())
//
//                .build();
//    }
//
    public static CollegeCourseResponseDto toResDto(CollegeCourse collegeCourse, CollegeResponseDto collegeResponseDto, CourseResponseDto courseResponseDto) {
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
                .build();
    }
//
//    public static List<CollegeCourseResponseDto> toResDto(List<CollegeCourse> collegeCourses) {
//        List<CollegeCourseResponseDto> collegeCourseResponseDtos = new ArrayList<>();
//        for (CollegeCourse collegeCourse : collegeCourses) {
//            collegeCourseResponseDtos.add(toResDto(collegeCourse, collegeCourse.getId(), collegeCourse.getCollege().getName(), collegeCourse.getCourse().getName()));
//        }
//        return collegeCourseResponseDtos;
//    }
//
//    public static void updateCollegeCourse(CollegeCourse collegeCourse, CollegeCourseRequestExcelDto collegeCourseRequestExcelDto) {
//        //collegeCourse.setIntakeMonth(collegeCourseRequestDto.getIntakeMonth());
//        collegeCourse.setIntakeYear(collegeCourseRequestExcelDto.getIntakeYear());
//        collegeCourse.setTuitionFee(collegeCourseRequestExcelDto.getTuitionFee());
//        collegeCourse.setApplicationFee(collegeCourseRequestExcelDto.getApplicationFee());
//        collegeCourse.setDuration(collegeCourseRequestExcelDto.getDuration());
//        collegeCourse.setApplicationDeadline(collegeCourseRequestExcelDto.getApplicationDeadline());
//        collegeCourse.setMaxStudents(collegeCourseRequestExcelDto.getMaxStudents());
//        collegeCourse.setStatus(collegeCourseRequestExcelDto.getStatus());
//    }
}
