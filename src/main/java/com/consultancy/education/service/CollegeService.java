package com.consultancy.education.service;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeCreateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;

public interface CollegeService {
    CollegeResponseDto create(CollegeCreateRequestDto req);
    CollegeResponseDto getById(Long id);
}
