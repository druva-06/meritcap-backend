package com.consultancy.education.service.impl;

import com.consultancy.education.DTOs.requestDTOs.user.UserRequestDto;
import com.consultancy.education.DTOs.responseDTOs.user.UserResponseDto;
import com.consultancy.education.enums.DocumentType;
import com.consultancy.education.exception.AlreadyExistException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.exception.ValidationException;
import com.consultancy.education.model.Student;
import com.consultancy.education.model.User;
import com.consultancy.education.repository.StudentRepository;
import com.consultancy.education.repository.UserRepository;
import com.consultancy.education.service.UserService;
import com.consultancy.education.transformer.UserTransformer;
import com.consultancy.education.validations.UserValidations;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private static final Log log = LogFactory.getLog(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final S3Client s3Client;
    private final String bucketName = "career-adivce-partner";

    public UserServiceImpl(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_2) // Update with your region
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
                        "***REMOVED***",
                        "***REMOVED***"
                )))
                .build();
    }
    @Override
    public UserResponseDto addUser(UserRequestDto userRequestDto) {
//        boolean existsByEmail = userRepository.existsByEmail(userRequestDto.getEmail());
//        boolean existsByPhoneNumber = userRepository.existsByPhoneNumber(userRequestDto.getPhoneNumber());
//        if(existsByEmail || existsByPhoneNumber){
//            List<String> errors = new ArrayList<>();
//            if(existsByEmail){
//                errors.add("Email already exists");
//            }
//            if(existsByPhoneNumber){
//                errors.add("Phone number already exists");
//            }
//            throw new AlreadyExistException(errors);
//        }
//        User user = UserTransformer.toEntity(userRequestDto);
//        user = userRepository.save(user);
//        return UserTransformer.toResDTO(user);
        return null;
    }

    @Override
    public UserResponseDto updateUser(UserRequestDto userRequestDto, Long userId) {
//        if(userRepository.findById(userId).isPresent()){
//            User user1 =  userRepository.findByEmail(userRequestDto.getEmail());
//            User user2 =  userRepository.findByPhoneNumber(userRequestDto.getPhoneNumber());
//            List<String> errors = UserValidations.checkEmailAndPhoneExist(userId, user1, user2);
//            if (!errors.isEmpty()) {
//                throw new AlreadyExistException(errors);
//            }
//            User user = userRepository.findById(userId).get();
//            UserTransformer.updateUser(user, userRequestDto);
//            user = userRepository.save(user);
//            return UserTransformer.toResDTO(user);
//        }
//        else{
//            throw new NotFoundException("User not found");
//        }
        return null;
    }

    @Override
    public UserResponseDto getUser(Long userId) {
//        if(userRepository.findById(userId).isPresent()){
//            return UserTransformer.toResDTO(userRepository.findById(userId).get());
//        }
//        else{
//            throw new NotFoundException("User not found");
//        }
        return null;
    }

    @Override
    public String uploadFile(Long userId, String documentType, MultipartFile file) {
        if(userRepository.findById(userId).isPresent()){
            User user = userRepository.findById(userId).get();
            // Convert MultipartFile to a File object
            try {
                File convertedFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
                file.transferTo(convertedFile);
                try {
                    try {
                        DocumentType.valueOf(documentType.toUpperCase()); // Safe call
                        // Construct the file key (folder structure inside S3)
                        String fileKey = String.format("students/%s/%s/%s", user.getUsername(), documentType.toUpperCase(), user.getUsername() + "_" + documentType.toUpperCase());

                        // Upload the file to S3
                        s3Client.putObject(PutObjectRequest.builder()
                                        .bucket(bucketName)
                                        .key(fileKey)
                                        .build(),
                                RequestBody.fromFile(convertedFile));

                        // Generate and return the URL of the uploaded file
                        String url = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileKey)).toExternalForm();
                        log.info("Uploaded File: " + url);

                        // Save document url in the database
                        Student student = user.getStudent();
                        switch (DocumentType.valueOf(documentType.toUpperCase())) {
                            case AADHAR -> student.setAadhaarCardFile(url);
                            case BIRTH -> student.setBirthCertificateFile(url);
                            case PAN -> student.setPanCardFile(url);
                            case PASSPORT -> student.setPassportFile(url);
                            default -> throw new IllegalArgumentException("Unsupported document type: " + documentType);
                        }

                        studentRepository.save(student);
                        return "Document uploaded successfully";
                    } catch (IllegalArgumentException e) {
                        log.error("Document type not supported: " + documentType);
                        return "Invalid document type";
                    }
                } catch (S3Exception e) {
                    log.error("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage());
                    throw new IOException(e.awsErrorDetails().errorMessage());
                }
            }
            catch(IOException e){
                log.error("Unable to convert file: " + file.getOriginalFilename());
                return "Something went wrong while uploading file";
            }
        }
        else {
            throw new NotFoundException("User with id " + userId + " not found");
        }
    }

}
