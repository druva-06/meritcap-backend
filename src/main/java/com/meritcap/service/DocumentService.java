package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.document.DocumentUploadRequestDto;
import com.meritcap.DTOs.responseDTOs.document.DocumentResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentResponseDto uploadDocument(DocumentUploadRequestDto requestDto, MultipartFile file, String uploadedBy);
    List<DocumentResponseDto> getDocuments(String referenceType, Long referenceId);
    void deleteDocument(Long documentId, String requestedBy);
    DocumentResponseDto uploadProfileImage(MultipartFile file, String uploadedBy);
    DocumentResponseDto updateDocumentStatus(Long documentId, String status, String reviewerRemarks);
    String generatePresignedUrl(Long documentId, String requestedByPrincipal);
}
