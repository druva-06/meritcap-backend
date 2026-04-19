package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.user.UserRequestDto;
import com.meritcap.DTOs.responseDTOs.invitation.InvitationResponseDto;
import com.meritcap.DTOs.responseDTOs.user.CounselorDto;
import com.meritcap.DTOs.responseDTOs.user.PagedUserResponseDto;
import com.meritcap.DTOs.responseDTOs.user.UserResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.exception.ValidationException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.UserService;
import com.meritcap.utils.ToMap;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("application/pdf", "image/jpeg", "image/png",
            "image/jpg");

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            InvitationResponseDto responseDto = userService.addUser(userRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(responseDto,
                            "Invitation sent successfully. User will receive signup link via email.", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody @Valid UserRequestDto userRequestDto, BindingResult bindingResult,
            Long userId) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }
        try {
            UserResponseDto responseDto = userService.updateUser(userRequestDto, userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(responseDto, "User updated successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(e.getErrors(), e.getMessage(), 409));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @GetMapping("/get")
    public ResponseEntity<?> getUser(@RequestParam Long userId) {
        try {
            UserResponseDto responseDto = userService.getUser(userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(responseDto, "User fetched successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PostMapping("/uploadFile/{userId}/{documentType}")
    public ResponseEntity<?> uploadFile(@PathVariable Long userId, @PathVariable String documentType,
            @RequestParam("file") MultipartFile file) {
        try {
            // Get the content type (MIME type) of the file
            String contentType = file.getContentType();

            // Check if the file's content type is in the allowed list
            if (!ALLOWED_FILE_TYPES.contains(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), "Invalid file format", 400));
            }

            String response = userService.uploadFile(userId, documentType, file);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(new ArrayList<>(), response, 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/counselors")
    public ResponseEntity<?> getAllCounselors() {
        log.info("Get all counselors request received");
        try {
            List<CounselorDto> counselors = userService.getAllCounselors();
            log.info("Successfully retrieved {} counselors", counselors.size());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(counselors, "Counselors fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching counselors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'COUNSELOR')")
    @GetMapping("/by-role")
    public ResponseEntity<?> getUsersByRole(
            @RequestParam String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Get users by role request received: role={}, page={}, size={}", role, page, size);
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), "Page number cannot be negative", 400));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), "Page size must be between 1 and 100", 400));
            }

            PagedUserResponseDto response = userService.getUsersByRoleName(role.toUpperCase(), page, size);
            log.info("Successfully retrieved {} users for role: {} (page {}/{})",
                    response.getUsers().size(), role, page + 1, response.getTotalPages());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(response, "Users fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching users by role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        log.info("Get all users request received: page={}, size={}, search={}", page, size, search);
        try {
            // Validate pagination parameters
            if (page < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), "Page number cannot be negative", 400));
            }
            if (size <= 0 || size > 100) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiFailureResponse<>(new ArrayList<>(), "Page size must be between 1 and 100", 400));
            }

            PagedUserResponseDto response = userService.getAllUsers(page, size, search);
            log.info("Successfully retrieved {} users (page {}/{})",
                    response.getUsers().size(), page + 1, response.getTotalPages());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(response, "Users fetched successfully", 200));
        } catch (Exception e) {
            log.error("Error fetching all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PatchMapping("/update-role")
    public ResponseEntity<?> updateUserRole(@RequestParam Long userId, @RequestParam String roleName) {
        log.info("Update user role request received: userId={}, roleName={}", userId, roleName);
        try {
            UserResponseDto responseDto = userService.updateUserRole(userId, roleName);
            log.info("Successfully updated role for user: {}", userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(responseDto, "User role updated successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error updating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        log.info("Delete user request received: userId={}", userId);
        try {
            userService.deleteUser(userId);
            log.info("Successfully deleted user: {}", userId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(null, "User deleted successfully", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}
