package com.meritcap.service;

import com.meritcap.DTOs.responseDTOs.bulk.BulkUploadResponseDto;
import com.meritcap.DTOs.responseDTOs.bulk.BulkUploadStatusDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BulkUploadService {
    BulkUploadResponseDto startBulkUpload(MultipartFile file, String currentUserId);
    BulkUploadStatusDto getStatus(Long jobId);
    List<BulkUploadStatusDto> listRecentJobs();
}
