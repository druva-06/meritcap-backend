package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.user.UserRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.CounselorDto;
import com.consultancy.education.DTOs.responseDTOs.user.PagedUserResponseDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import com.consultancy.education.enums.DocumentType;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.model.Role;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.RoleRepository;
import com.consultancy.education.repository.StudentRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.service.UserService;
import com.consultancy.education.transformer.UserTransformer;
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
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKeyId}")
    private String accessKeyId;

    @Value("${aws.s3.secretAccessKey}")
    private String secretAccessKey;

    public UserServiceImpl(UserRepository userRepository, StudentRepository studentRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initS3Client() {
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_2)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();
    }

    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
        // boolean existsByEmail =
        // userRepository.existsByEmail(userRequestDto.getEmail());
        // boolean existsByPhoneNumber =
        // userRepository.existsByPhoneNumber(userRequestDto.getPhoneNumber());
        // if(existsByEmail || existsByPhoneNumber){
        // List<String> errors = new ArrayList<>();
        // if(existsByEmail){
        // errors.add("Email already exists");
        // }
        // if(existsByPhoneNumber){
        // errors.add("Phone number already exists");
        // }
        // throw new AlreadyExistException(errors);
        // }
        // User user = UserTransformer.toEntity(userRequestDto);
        // user = userRepository.save(user);
        // return UserTransformer.toResDTO(user);
        return null;
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
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        userRepository.delete(user);
        log.info("User deleted successfully: " + userId);
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
