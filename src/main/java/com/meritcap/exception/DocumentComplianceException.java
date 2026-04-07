package com.meritcap.exception;

import com.meritcap.DTOs.responseDTOs.documentconfig.DocumentComplianceResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class DocumentComplianceException extends RuntimeException {

    private final List<DocumentComplianceResponseDto.MissingDocument> missingDocuments;

    public DocumentComplianceException(List<DocumentComplianceResponseDto.MissingDocument> missingDocuments) {
        super("Missing required documents for application");
        this.missingDocuments = missingDocuments;
    }
}
