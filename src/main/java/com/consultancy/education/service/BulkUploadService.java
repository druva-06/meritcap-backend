package com.consultancy.education.service;

import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadResponseDto;
import com.consultancy.education.DTOs.responseDTOs.bulk.BulkUploadStatusDto;
import org.springframework.web.multipart.MultipartFile;

public interface BulkUploadService {
    BulkUploadResponseDto startBulkUpload(MultipartFile file, String currentUserId);
    BulkUploadStatusDto getStatus(Long jobId);
}
