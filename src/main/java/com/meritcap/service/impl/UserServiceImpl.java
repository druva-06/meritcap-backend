package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.invitation.InvitationRequestDto;
import com.meritcap.DTOs.requestDTOs.user.UserRequestDto;
import com.meritcap.DTOs.responseDTOs.invitation.InvitationResponseDto;
import com.meritcap.DTOs.responseDTOs.user.CounselorDto;
import com.meritcap.DTOs.responseDTOs.user.PagedUserResponseDto;
import com.meritcap.DTOs.responseDTOs.user.UserResponseDto;
import com.meritcap.enums.DocumentType;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Role;
import com.meritcap.model.User;
import com.meritcap.repository.RoleRepository;
import com.meritcap.repository.StudentRepository;
import com.meritcap.repository.UserRepository;
import com.meritcap.repository.DocumentRepository;
import com.meritcap.repository.EmailOTPRepository;
import com.meritcap.repository.InvitedUserRepository;
import com.meritcap.repository.LeadRepository;
import com.meritcap.repository.ScholarshipRepository;
import com.meritcap.repository.StudentCollegeCourseRegistrationRepository;
import com.meritcap.service.CognitoService;
import com.meritcap.service.InvitationService;
import com.meritcap.service.UserService;
import com.meritcap.transformer.UserTransformer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final InvitationService invitationService;
    private final CognitoService cognitoService;
    private final DocumentRepository documentRepository;
    private final EmailOTPRepository emailOTPRepository;
    private final InvitedUserRepository invitedUserRepository;
    private final LeadRepository leadRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final StudentCollegeCourseRegistrationRepository studentCollegeCourseRegistrationRepository;
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.s3.secretAccessKey}")
    private String secretAccessKey;

    public UserServiceImpl(UserRepository userRepository, StudentRepository studentRepository,
            RoleRepository roleRepository, InvitationService invitationService, CognitoService cognitoService,
            DocumentRepository documentRepository, EmailOTPRepository emailOTPRepository,
            InvitedUserRepository invitedUserRepository, LeadRepository leadRepository,
            ScholarshipRepository scholarshipRepository,
            StudentCollegeCourseRegistrationRepository studentCollegeCourseRegistrationRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.invitationService = invitationService;
        this.cognitoService = cognitoService;
        this.documentRepository = documentRepository;
        this.emailOTPRepository = emailOTPRepository;
        this.invitedUserRepository = invitedUserRepository;
        this.leadRepository = leadRepository;
        this.scholarshipRepository = scholarshipRepository;
        this.studentCollegeCourseRegistrationRepository = studentCollegeCourseRegistrationRepository;
    }

    @PostConstruct
    public void initS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }

    @Transactional
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        log.info("Creating user invitation for email: {}", userRequestDto.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(userRequestDto.getEmail())) {
            throw new AlreadyExistException(List.of("User with this email already exists"));
        }

        if (userRepository.existsByPhoneNumber(userRequestDto.getPhoneNumber())) {
            throw new AlreadyExistException(List.of("User with this phone number already exists"));
        }

        // Parse name into first and last name
        String[] nameParts = userRequestDto.getName().trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        // Create invitation request
        InvitationRequestDto invitationRequest = InvitationRequestDto.builder()
                .email(userRequestDto.getEmail())
                .roleName(userRequestDto.getRoleName())
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(userRequestDto.getPhoneNumber())
                .expiryDays(7) // Default 7 days expiry
                .build();

        // Create invitation using InvitationService
        // TODO: Get actual logged-in user ID from security context
        Long invitedByUserId = 1L; // Placeholder - should be extracted from authentication

        InvitationResponseDto invitation = invitationService.createInvitation(invitationRequest, invitedByUserId);

        log.info("Invitation created successfully. User will receive signup link via email.");

        // Return a response indicating invitation was sent
        // Note: User is not actually created yet, will be created when they sign up
        return UserResponseDto.builder()
                .email(userRequestDto.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(userRequestDto.getPhoneNumber())
                .role(userRequestDto.getRoleName())
                .build();
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId) {
        // if(userRepository.findById(userId).isPresent()){
        // User user1 = userRepository.findByEmail(userRequestDto.getEmail());
        // User user2 =
        // userRepository.findByPhoneNumber(userRequestDto.getPhoneNumber());
        // List<String> errors = UserValidations.checkEmailAndPhoneExist(userId, user1,
        // user2);
        // if (!errors.isEmpty()) {
        // throw new AlreadyExistException(errors);
        // }
        // User user = userRepository.findById(userId).get();
        // UserTransformer.updateUser(user, userRequestDto);
        // user = userRepository.save(user);
        // return UserTransformer.toResDTO(user);
        // }
        // else{
        // throw new NotFoundException("User not found");
        // }
        return null;
    }

    @Override
    public UserResponseDto getUser(Long userId) {
        // if(userRepository.findById(userId).isPresent()){
        // return UserTransformer.toResDTO(userRepository.findById(userId).get());
        // }
        // else{
        // throw new NotFoundException("User not found");
        // }
        return null;
    }

    @Override
    public String uploadFile(Long userId, String documentType, MultipartFile file) {
        if (userRepository.findById(userId).isPresent()) {
            User user = userRepository.findById(userId).get();
            // Convert MultipartFile to a File object
            try {
                File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
                file.transferTo(convertedFile);
                try {
                    try {
                        DocumentType.valueOf(documentType.toUpperCase()); // Safe call
                        // Construct the file key (folder structure inside S3)
                        String fileKey = String.format("students/%s/%s/%s", user.getUsername(),
                                documentType.toUpperCase(), user.getUsername() + "_" + documentType.toUpperCase());

                        // Upload the file to S3
                        s3Client.putObject(PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileKey)
                                .build(),
                                RequestBody.fromFile(convertedFile));

                        // Generate and return the URL of the uploaded file
                        String url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileKey))
                                .toExternalForm();
                        log.info("Uploaded File: " + url);

                        return "Document uploaded successfully";
                    } catch (IllegalArgumentException e) {
                        log.error("Document type not supported: " + documentType);
                        return "Invalid document type";
                    }
                } catch (S3Exception e) {
                    log.error("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());
                    throw new IOException(e.awsErrorDetails().errorMessage());
                }
            } catch (IOException e) {
                log.error("Unable to convert file: " + file.getOriginalFilename());
                return "Something went wrong while uploading file";
            }
        } else {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }

    @Override
    public List<CounselorDto> getAllCounselors() {
        log.info("Fetching all counselors");
        List<User> counselors = userRepository.findByRoleName("COUNSELOR");
        log.info("Found " + counselors.size() + " counselors");

        List<CounselorDto> counselorDtos = counselors.stream()
                .map(counselor -> CounselorDto.builder()
                        .id(counselor.getId())
                        .name(counselor.getFirstName() + " " + counselor.getLastName())
                        .email(counselor.getEmail())
                        .phoneNumber(counselor.getPhoneNumber())
                        .build())
                .collect(Collectors.toList());

        log.info("Successfully mapped " + counselorDtos.size() + " counselors to DTOs");
        return counselorDtos;
    }

    @Override
    public PagedUserResponseDto getUsersByRole(Role role, int page, int size) {
        log.info("Fetching users by role: " + role.getName() + " with page: " + page + " and size: " + size);

        // Create pageable with sorting by createdAt descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Fetch paginated users
        Page<User> userPage = userRepository.findByRole(role, pageable);

        // Convert to DTOs
        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(UserTransformer::toUserResponseDto)
                .collect(Collectors.toList());

        // Build paginated response
        PagedUserResponseDto response = PagedUserResponseDto.builder()
                .users(userDtos)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();

        log.info("Found " + userDtos.size() + " users for role: " + role.getName() + " (page " + (page + 1) + "/"
                + userPage.getTotalPages() + ")");

        return response;
    }

    @Override
    public PagedUserResponseDto getUsersByRoleName(String roleName, int page, int size) {
        log.info("Fetching users by role name: " + roleName + " with page: " + page + " and size: " + size);

        // Create pageable with sorting by createdAt descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Fetch paginated users
        Page<User> userPage = userRepository.findByRoleName(roleName, pageable);

        // Convert to DTOs
        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(UserTransformer::toUserResponseDto)
                .collect(Collectors.toList());

        // Build paginated response
        PagedUserResponseDto response = PagedUserResponseDto.builder()
                .users(userDtos)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();

        log.info("Found " + userDtos.size() + " users for role: " + roleName + " (page " + (page + 1) + "/"
                + userPage.getTotalPages() + ")");

        return response;
    }

    @Override
    public PagedUserResponseDto getAllUsers(int page, int size, String search) {
        log.info("Fetching all users with page: " + page + ", size: " + size + ", search: " + search);

        // Create pageable with sorting by createdAt descending
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> userPage;
        if (search != null && !search.trim().isEmpty()) {
            // Search in firstName, lastName, email, or username
            userPage = userRepository.searchUsers(search.trim(), pageable);
        } else {
            // Fetch all users
            userPage = userRepository.findAll(pageable);
        }

        // Convert to DTOs
        List<UserResponseDto> userDtos = userPage.getContent().stream()
                .map(UserTransformer::toUserResponseDto)
                .collect(Collectors.toList());

        // Build paginated response
        PagedUserResponseDto response = PagedUserResponseDto.builder()
                .users(userDtos)
                .currentPage(userPage.getNumber())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .pageSize(userPage.getSize())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();

        log.info("Found " + userDtos.size() + " users (page " + (page + 1) + "/" + userPage.getTotalPages() + ")");

        return response;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        String email = user.getEmail();
        Long studentId = user.getStudent() != null ? user.getStudent().getId() : null;

        // Break non-cascading references that would block deletion.
        leadRepository.clearAssignedToReferences(userId);
        leadRepository.clearCreatedByReferences(userId);
        invitedUserRepository.clearUserReference(userId, LocalDateTime.now());
        invitedUserRepository.deleteAllByInvitedById(userId);
        studentCollegeCourseRegistrationRepository.clearAssignedCounselorReferences(userId);
        scholarshipRepository.deleteAllByUserId(userId);
        emailOTPRepository.deleteAllByEmail(email);

        // Physical cleanup in S3 + DB for documents directly linked to this user/student.
        deleteAllDocumentsAndS3Objects("USER", userId);
        if (studentId != null) {
            deleteAllDocumentsAndS3Objects("STUDENT", studentId);
        }

        // Remove invitation record tied to this final user account.
        invitedUserRepository.deleteAllByUserId(userId);

        // Remove direct M:N links explicitly before deleting user.
        user.getAdditionalPermissions().clear();
        userRepository.save(user);
        userRepository.flush();

        // Delete Cognito first so DB deletion rolls back if it fails.
        try {
            cognitoService.deleteUser(email);
            log.info("User deleted from Cognito: {}", email);
        } catch (NotFoundException ex) {
            log.warn("User not found in Cognito while deleting userId {}: {}", userId, email);
        } catch (Exception ex) {
            log.error("Failed to delete user {} from Cognito. Rolling back DB deletion.", userId, ex);
            throw ex;
        }

        // Delete user from DB (student/profile graph is removed via mapped cascades).
        userRepository.delete(user);
        userRepository.flush();
        log.info("User deleted completely from DB and Cognito: {}", userId);
    }

    private void deleteAllDocumentsAndS3Objects(String referenceType, Long referenceId) {
        List<com.meritcap.model.Document> documents = documentRepository.findAllByReferenceTypeAndReferenceId(referenceType, referenceId);
        for (com.meritcap.model.Document document : documents) {
            deleteS3ObjectByUrl(document.getFileUrl());
        }
        documentRepository.deleteAllByReferenceTypeAndReferenceId(referenceType, referenceId);
        log.info("Deleted {} documents for {}:{}", documents.size(), referenceType, referenceId);
    }

    private void deleteS3ObjectByUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        String objectKey = extractS3Key(fileUrl);
        if (objectKey == null || objectKey.isBlank()) {
            log.warn("Unable to derive S3 key from URL: {}", fileUrl);
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (Exception ex) {
            log.error("Failed to delete S3 object for key {}", objectKey, ex);
            throw new RuntimeException("Failed to delete S3 object for user cleanup", ex);
        }
    }

    private String extractS3Key(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            String host = Objects.toString(uri.getHost(), "");
            String path = Objects.toString(uri.getPath(), "");
            if (host.startsWith(bucketName + ".")) {
                return path.startsWith("/") ? path.substring(1) : path;
            }
            String bucketPrefix = "/" + bucketName + "/";
            if (path.startsWith(bucketPrefix)) {
                return path.substring(bucketPrefix.length());
            }
            return null;
        } catch (Exception ex) {
            log.warn("Invalid S3 URL format: {}", fileUrl);
            return null;
        }
    }

    @Override
    @Transactional
    public UserResponseDto updateUserRole(Long userId, String roleName) {
        log.info("Updating role for user: {}, new role: {}", userId, roleName);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new NotFoundException("Role not found with name: " + roleName));

        if (!role.getIsActive()) {
            throw new IllegalArgumentException("Cannot assign inactive role: " + roleName);
        }

        user.setRole(role);
        User updatedUser = userRepository.save(user);

        log.info("User role updated successfully: userId={}, newRole={}", userId, roleName);
        return UserTransformer.toUserResponseDto(updatedUser);
    }

}
