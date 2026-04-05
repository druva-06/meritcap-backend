package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.document.DocumentUploadRequestDto;
import com.meritcap.DTOs.responseDTOs.document.DocumentResponseDto;
import com.meritcap.enums.DocumentStatus;
import com.meritcap.exception.CustomException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.exception.ValidationException;
import com.meritcap.model.Document;
import com.meritcap.model.User;
import com.meritcap.repository.DocumentRepository;
import com.meritcap.repository.StudentRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;

import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final CognitoIdentityProviderClient cognitoClient;
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;
    @Value("${aws.s3.accessKeyId}")
    private String accessKeyId;
    @Value("${aws.s3.secretAccessKey}")
    private String secretAccessKey;
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/jpg",
            "video/mp4", "audio/mpeg"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public DocumentServiceImpl(DocumentRepository documentRepository,
                               StudentRepository studentRepository,
                               UserRepository userRepository,
                               CognitoIdentityProviderClient cognitoClient) {
        this.documentRepository = documentRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.cognitoClient = cognitoClient;
    }


    @PostConstruct
    public void initS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                ))
                .build();
    }

    @Override
    @Transactional
    public DocumentResponseDto uploadDocument(DocumentUploadRequestDto requestDto, MultipartFile file, String uploadedBy) {
        User requester = resolveUserByPrincipal(uploadedBy);
        String canonicalUploadedBy = requester != null && requester.getEmail() != null
                ? requester.getEmail()
                : uploadedBy;

        log.info("Received upload request: referenceType={}, referenceId={}, documentType={}, category={}, uploadedBy={}",
                requestDto.getReferenceType(), requestDto.getReferenceId(), requestDto.getDocumentType(),
                requestDto.getCategory(), uploadedBy);

        // Validate inputs
        validateRequest(requestDto, file);

        if ("STUDENT".equalsIgnoreCase(requestDto.getReferenceType()) && requester != null && !isAdmin(requester)) {
            if (requester.getStudent() == null || requester.getStudent().getId() == null) {
                throw new CustomException("You do not have permission to access this resource");
            }
            if (!requester.getStudent().getId().equals(requestDto.getReferenceId())) {
                throw new CustomException("You do not have permission to access this resource");
            }
        }

        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        long fileSize = file.getSize();
        log.debug("File meta - originalName='{}', contentType='{}', sizeBytes={}",
                file.getOriginalFilename(), contentType, fileSize);

        // Validate reference existence for STUDENT (adjust for other types if needed)
        if ("STUDENT".equalsIgnoreCase(requestDto.getReferenceType())) {
            boolean exists = studentRepository.existsById(requestDto.getReferenceId());
            if (!exists) {
                log.warn("Student not found: id={}", requestDto.getReferenceId());
                throw new NotFoundException("Student not found");
            }
            log.debug("Student exists: id={}", requestDto.getReferenceId());
        }

        // Soft-delete any existing active document for same reference/documentType
        documentRepository.findByReferenceTypeAndReferenceIdAndDocumentTypeAndIsDeletedFalse(
                requestDto.getReferenceType(),
                requestDto.getReferenceId(),
                requestDto.getDocumentType()
        ).ifPresent(existing -> {
            log.info("Found existing active document id={}; marking as deleted", existing.getId());
            existing.setIsDeleted(true);
            documentRepository.save(existing);
            log.debug("Existing document id={} marked deleted", existing.getId());
        });

        // Build safe S3 key
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String extension = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < originalName.length() - 1) {
            extension = originalName.substring(lastDot);
        }
        String sanitizedReferenceType = sanitizePathComponent(requestDto.getReferenceType());
        String sanitizedDocumentType = sanitizePathComponent(requestDto.getDocumentType());
        String fileKey = String.format("%s/%d/%s/%s_%d%s",
                sanitizedReferenceType,
                requestDto.getReferenceId(),
                sanitizedDocumentType,
                sanitizedDocumentType,
                System.currentTimeMillis(),
                extension
        );
        log.debug("Constructed S3 fileKey='{}'", fileKey);

        // Stream upload to S3
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            log.info("Uploading to S3. bucket='{}' key='{}'", bucketName, fileKey);
            s3Client.putObject(putReq, RequestBody.fromInputStream(in, fileSize));
            log.info("S3 upload successful. key='{}'", fileKey);
        } catch (Exception e) {
            log.error("Failed to upload file to S3. referenceType={}, referenceId={}, documentType={}, key={}",
                    requestDto.getReferenceType(), requestDto.getReferenceId(), requestDto.getDocumentType(), fileKey, e);
            throw new RuntimeException("Error while uploading document to S3", e);
        }

        // Build file URL (uses S3 utilities)
        String fileUrl = s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileKey)).toExternalForm();
        log.debug("Resolved S3 file URL='{}' for key='{}'", fileUrl, fileKey);

        // Create Document entity
        Document document = Document.builder()
                .referenceType(requestDto.getReferenceType())
                .referenceId(requestDto.getReferenceId())
                .documentType(requestDto.getDocumentType())
                .category(requestDto.getCategory())
                .remarks(requestDto.getRemarks())
                .documentStatus(DocumentStatus.PENDING)
                .fileUrl(fileUrl)
                .uploadedBy(canonicalUploadedBy)
                .isDeleted(false)
                .build();

        // Persist entity. If DB save fails, attempt S3 cleanup
        try {
            document = documentRepository.save(document);
            log.info("Document saved to DB with id={}", document.getId());
        } catch (Exception dbEx) {
            log.error("DB save failed after S3 upload. Attempting to delete S3 object key='{}' to avoid orphan", fileKey, dbEx);
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build());
                log.info("Deleted uploaded S3 object key='{}' after DB failure", fileKey);
            } catch (Exception delEx) {
                // cleanup best-effort: log but don't mask original exception
                log.error("Failed to delete S3 object key='{}' after DB failure; manual cleanup may be required", fileKey, delEx);
            }
            throw dbEx;
        }

        // Build response DTO
        DocumentResponseDto response = DocumentResponseDto.builder()
                .id(document.getId())
                .referenceType(document.getReferenceType())
                .referenceId(document.getReferenceId())
                .documentType(document.getDocumentType())
                .category(document.getCategory())
                .remarks(document.getRemarks())
                .documentStatus(document.getDocumentStatus().toString())
                .fileUrl(document.getFileUrl())
                .uploadedBy(document.getUploadedBy())
                .uploadedAt(document.getUploadedAt() != null ? document.getUploadedAt() : Instant.now())
                .build();

        log.info("Upload flow complete: documentId={}, referenceType={}, referenceId={}",
                response.getId(), response.getReferenceType(), response.getReferenceId());

        return response;
    }

    // -------------------------
    // Helper validations & utils
    // -------------------------

    private void validateRequest(DocumentUploadRequestDto requestDto, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation failed: file missing/empty");
            throw new ValidationException(Collections.singletonList("File must not be empty"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Validation failed: unsupported content-type='{}'", contentType);
            throw new ValidationException(Collections.singletonList("Unsupported file type: " + contentType));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Validation failed: file size {} exceeds max {}", file.getSize(), MAX_FILE_SIZE);
            throw new ValidationException(Collections.singletonList("File size exceeds " + MAX_FILE_SIZE + " bytes limit"));
        }

        if (requestDto.getReferenceType() == null || requestDto.getReferenceType().isBlank()) {
            log.warn("Validation failed: referenceType missing");
            throw new ValidationException(Collections.singletonList("Reference type is required"));
        }

        if (requestDto.getReferenceId() == null) {
            log.warn("Validation failed: referenceId missing");
            throw new ValidationException(Collections.singletonList("Reference id is required"));
        }

        if (requestDto.getDocumentType() == null || requestDto.getDocumentType().isBlank()) {
            log.warn("Validation failed: documentType missing");
            throw new ValidationException(Collections.singletonList("Document type is required"));
        }

        if (requestDto.getCategory() == null || requestDto.getCategory().isBlank()) {
            log.warn("Validation failed: category missing");
            throw new ValidationException(Collections.singletonList("Category is required"));
        }
    }

    /**
     * Sanitize a path component to be safe for S3 keys (lowercase alphanum + hyphen).
     */
    private String sanitizePathComponent(String input) {
        if (input == null) return "unknown";
        return input.toLowerCase().replaceAll("[^a-z0-9\\-]", "_");
    }
    @Override
    public List<DocumentResponseDto> getDocuments(String referenceType, Long referenceId) {
        log.info("Fetching documents for referenceType={}, referenceId={}", referenceType, referenceId);
        List<Document> docs = documentRepository.findAllByReferenceTypeAndReferenceIdAndIsDeletedFalse(referenceType, referenceId);
        List<DocumentResponseDto> responseDtos = new ArrayList<>();
        for (Document doc : docs) {
            responseDtos.add(DocumentResponseDto.builder()
                    .id(doc.getId())
                    .referenceType(doc.getReferenceType())
                    .referenceId(doc.getReferenceId())
                    .documentType(doc.getDocumentType())
                    .remarks(doc.getRemarks())
                    .category(doc.getCategory())
                    .documentStatus(doc.getDocumentStatus().toString())
                    .fileUrl(doc.getFileUrl())
                    .uploadedBy(doc.getUploadedBy())
                    .uploadedAt(doc.getUploadedAt())
                    .build());
        }
        return responseDtos;
    }

    @Override
    public void deleteDocument(Long documentId, String requestedBy) {
        log.info("Delete document request for id={}, by={}", documentId, requestedBy);

        User requester = resolveUserByPrincipal(requestedBy);
        if (requester == null) {
            throw new NotFoundException("User not found");
        }

        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (!isAdmin(requester)) {
            if (!"STUDENT".equalsIgnoreCase(doc.getReferenceType())
                    || requester.getStudent() == null
                    || requester.getStudent().getId() == null
                    || !requester.getStudent().getId().equals(doc.getReferenceId())) {
                throw new CustomException("You do not have permission to access this resource");
            }
        }

        doc.setIsDeleted(true);
        documentRepository.save(doc);
        log.info("Document marked as deleted: {}", documentId);
    }

    @Override
    @Transactional
    public DocumentResponseDto uploadProfileImage(MultipartFile file, String uploadedBy) {
        log.info("Profile image upload requested by {}", uploadedBy);

        // validate file like before (image types and 5MB)
        if (file == null || file.isEmpty()) {
            throw new ValidationException(Collections.singletonList("File must not be empty"));
        }
        String contentType = Optional.ofNullable(file.getContentType()).orElse("");
        Set<String> allowedImageMime = Set.of("image/jpeg", "image/png", "image/jpg", "image/webp");
        if (!allowedImageMime.contains(contentType.toLowerCase())) {
            throw new ValidationException(Collections.singletonList("Unsupported file type: " + contentType));
        }
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new ValidationException(Collections.singletonList("File size exceeds 5MB"));
        }

        // find local user and student id
        User user = resolveUserByPrincipal(uploadedBy);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        if (user.getStudent() == null || user.getStudent().getId() == null) {
            throw new CustomException("Authenticated student not found");
        }
        Long studentId = user.getStudent().getId();
        String canonicalUploadedBy = user.getEmail() != null ? user.getEmail() : uploadedBy;

        // soft-delete existing active PROFILE_IMAGE doc
        documentRepository.findByReferenceTypeAndReferenceIdAndDocumentTypeAndIsDeletedFalse(
                "STUDENT", studentId, "PROFILE_IMAGE"
        ).ifPresent(existing -> {
            existing.setIsDeleted(true);
            documentRepository.save(existing);
            log.info("Soft-deleted old profile document id={}", existing.getId());
        });

        // prepare a DocumentUploadRequestDto for profile image
        DocumentUploadRequestDto reqDto = DocumentUploadRequestDto.builder()
                .referenceType("STUDENT")
                .referenceId(studentId)
                .documentType("PROFILE_IMAGE")
                .category("Personal")
                .remarks(null)
                .build();

        // 1) upload to S3 & save Document (via helper)
        Map<String, Object> stored = storeDocumentToS3AndDb(reqDto, file, canonicalUploadedBy);
        Document savedDoc = (Document) stored.get("document");
        String newKey = (String) stored.get("fileKey");
        String newUrl = (String) stored.get("fileUrl");

        // record previous URL/key to delete after success
        String prevUrl = user.getProfilePicture();
        String prevKey = prevUrl != null && !prevUrl.isBlank() ? extractKeyFromS3Url(prevUrl) : null;

        // 2) update user.profile_picture in DB (still inside this @Transactional method)
        user.setProfilePicture(newUrl);
        userRepository.save(user);
        log.info("Updated user.profile_picture for userId={}", user.getId());

        // 3) update Cognito attribute (admin). If this fails, roll back transaction and delete new S3 object.
        try {
            AttributeType pictureAttr = AttributeType.builder().name("picture").value(newUrl).build();
            AdminUpdateUserAttributesRequest req = AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId)
                    .username(user.getEmail())
                    .userAttributes(pictureAttr)
                    .build();
            cognitoClient.adminUpdateUserAttributes(req);
            log.info("Cognito picture attribute updated for {}", user.getEmail());

            // Cognito succeeded -> attempt to delete previous S3 object (best-effort)
            if (prevKey != null) {
                try {
                    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(prevKey).build());
                    log.info("Deleted previous S3 object key={}", prevKey);
                } catch (Exception ex) {
                    log.warn("Failed to delete previous S3 object key={}, manual cleanup may be required", prevKey);
                }
            }

            // return response DTO built from savedDoc
            return DocumentResponseDto.builder()
                    .id(savedDoc.getId())
                    .referenceType(savedDoc.getReferenceType())
                    .referenceId(savedDoc.getReferenceId())
                    .documentType(savedDoc.getDocumentType())
                    .category(savedDoc.getCategory())
                    .remarks(savedDoc.getRemarks())
                    .documentStatus(savedDoc.getDocumentStatus().toString())
                    .fileUrl(savedDoc.getFileUrl())
                    .uploadedBy(savedDoc.getUploadedBy())
                    .uploadedAt(savedDoc.getUploadedAt())
                    .build();

        } catch (Exception cognitoEx) {
            log.error("Cognito update failed: {} — attempting to delete new S3 object and roll back", cognitoEx.getMessage());

            // best-effort delete the newly uploaded object
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(newKey).build());
                log.info("Deleted newly uploaded S3 object key={} after Cognito failure", newKey);
            } catch (Exception delEx) {
                log.error("Failed to delete newly uploaded object key={} after Cognito failure; manual cleanup required", newKey, delEx);
                // optionally: insert into pending-delete table for later cleanup
            }

            // throw runtime so transaction will rollback (document and user update will be rolled back)
            throw new CustomException("Failed to update Cognito attribute: " + cognitoEx.getMessage());
        }
    }

    /**
     * Uploads file to S3 and saves a Document entity (but does NOT touch User or Cognito).
     * Returns the saved Document and the S3 key in a small holder Map.
     */
    private Map<String, Object> storeDocumentToS3AndDb(DocumentUploadRequestDto requestDto,
                                                       MultipartFile file,
                                                       String uploadedBy) {
        // copied & slightly adapted logic from existing uploadDocument(...)
        String contentType = Optional.ofNullable(file.getContentType()).orElse("application/octet-stream");
        long fileSize = file.getSize();
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("file");
        String extension = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < originalName.length() - 1) {
            extension = originalName.substring(lastDot);
        }

        String sanitizedReferenceType = sanitizePathComponent(requestDto.getReferenceType());
        String sanitizedDocumentType = sanitizePathComponent(requestDto.getDocumentType());
        String fileKey = String.format("%s/%d/%s/%s_%d%s",
                sanitizedReferenceType,
                requestDto.getReferenceId(),
                sanitizedDocumentType,
                sanitizedDocumentType,
                System.currentTimeMillis(),
                extension
        );

        // stream upload to S3 (no ACL)
        try (InputStream in = file.getInputStream()) {
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            s3Client.putObject(putReq, RequestBody.fromInputStream(in, fileSize));
            log.info("S3 upload successful. key='{}'", fileKey);
        } catch (Exception e) {
            log.error("Failed to upload file to S3. key={}", fileKey, e);
            throw new RuntimeException("Error while uploading document to S3", e);
        }

        // build URL
        String fileUrl;
        try {
            fileUrl = s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(fileKey)).toExternalForm();
        } catch (Exception e) {
            log.warn("S3 utilities getUrl failed; fallback URL built");
            fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileKey);
        }

        // create Document entity
        Document document = Document.builder()
                .referenceType(requestDto.getReferenceType())
                .referenceId(requestDto.getReferenceId())
                .documentType(requestDto.getDocumentType())
                .category(requestDto.getCategory())
                .remarks(requestDto.getRemarks())
                .documentStatus(DocumentStatus.VERIFIED)
                .fileUrl(fileUrl)
                .uploadedBy(uploadedBy)
                .isDeleted(false)
                .build();

        // persist doc; if DB save fails, delete S3 object to avoid orphan
        try {
            document = documentRepository.save(document);
            log.info("Document saved to DB with id={}", document.getId());
        } catch (Exception dbEx) {
            log.error("DB save failed after S3 upload. Attempting to delete S3 object key='{}' to avoid orphan", fileKey, dbEx);
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build());
                log.info("Deleted uploaded S3 object key='{}' after DB failure", fileKey);
            } catch (Exception delEx) {
                log.error("Failed to delete S3 object key='{}' after DB failure; manual cleanup required", fileKey, delEx);
            }
            throw dbEx;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("document", document);
        result.put("fileKey", fileKey);
        result.put("fileUrl", fileUrl);
        return result;
    }

    private String extractKeyFromS3Url(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost() != null ? uri.getHost() : "";
            String path = uri.getPath() != null ? uri.getPath() : "";
            // Case: bucket in host (bucket.s3.amazonaws.com) -> path starts with /{key}
            if (host.startsWith(bucketName + ".")) {
                if (path.startsWith("/")) path = path.substring(1);
                return path;
            }
            // Case: s3.region.amazonaws.com -> path starts with /{bucket}/{key}
            if (path.startsWith("/" + bucketName + "/")) {
                return path.substring(("/" + bucketName + "/").length());
            }
            // Last resort: if path contains bucketName, remove prefix until key
            int idx = path.indexOf("/" + bucketName + "/");
            if (idx >= 0) {
                return path.substring(idx + ("/" + bucketName + "/").length());
            }
            // if none matched, return null
            return null;
        } catch (Exception e) {
            log.debug("Failed to parse S3 URL to key: {}", e.getMessage());
            return null;
        }
    }

    private User resolveUserByPrincipal(String principal) {
        if (principal == null || principal.isBlank()) {
            return null;
        }
        if (principal.startsWith("otp-user-")) {
            String idPart = principal.substring("otp-user-".length());
            try {
                Long userId = Long.parseLong(idPart);
                return userRepository.findById(userId).orElse(null);
            } catch (NumberFormatException ignored) {
                // fallback to email/username matching
            }
        }
        User user = userRepository.findByEmailIgnoreCase(principal);
        if (user == null) {
            user = userRepository.findByUsernameIgnoreCase(principal);
        }
        if (user == null && principal.contains("@")) {
            user = userRepository.findByEmail(principal.toLowerCase(Locale.ROOT));
        }
        return user;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole().getName() != null
                && "ADMIN".equalsIgnoreCase(user.getRole().getName());
    }
}
