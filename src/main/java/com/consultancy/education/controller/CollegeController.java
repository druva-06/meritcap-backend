package com.consultancy.education.controller;

import com.consultancy.education.DTOs.requestDTOs.college.CollegeCreateRequestDto;
import com.consultancy.education.DTOs.responseDTOs.college.CollegeResponseDto;
import com.consultancy.education.exception.CustomException;
import com.consultancy.education.exception.NotFoundException;
import com.consultancy.education.response.ApiFailureResponse;
import com.consultancy.education.response.ApiSuccessResponse;
import com.consultancy.education.service.CollegeService;
import com.consultancy.education.utils.ToMap;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/colleges")
@Tag(name = "College Controller", description = "Handles College operations")
public class CollegeController {

    private final CollegeService collegeService;

    public CollegeController(CollegeService collegeService) {
        this.collegeService = collegeService;
    }

    // ------------------ Create College ------------------
    @Operation(
            summary = "Create a college",
            description = "Creates a new college. `campusCode` and `slug` must be unique."
    )
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = CollegeResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Validation/Business error",
            content = @Content(schema = @Schema(implementation = ApiFailureResponse.class)))
    @ApiResponse(responseCode = "500", description = "Server error",
            content = @Content(schema = @Schema(implementation = ApiFailureResponse.class)))
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid CollegeCreateRequestDto request,
                                    BindingResult bindingResult) {
        log.info("CreateCollege request received: slug={}, campusCode={}, name={}",
                request.getSlug(), request.getCampusCode(), request.getName());

        if (bindingResult.hasErrors()) {
            log.error("CreateCollege validation errors");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiFailureResponse<>(ToMap.bindingResultToMap(bindingResult), "Validation failed", 400));
        }

        try {
            CollegeResponseDto response = collegeService.create(request);
            log.info("CreateCollege success: id={}, slug={}, campusCode={}",
                    response.getId(), response.getSlug(), response.getCampusCode());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(response, "College created successfully!", 201));
        } catch (CustomException e) {
            log.error("CreateCollege business error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("CreateCollege internal error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }

    // ------------------ Get College by ID ------------------
    @Operation(
            summary = "Get college by ID",
            description = "Returns a college by its ID."
    )
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = CollegeResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Not Found",
            content = @Content(schema = @Schema(implementation = ApiFailureResponse.class)))
    @ApiResponse(responseCode = "500", description = "Server error",
            content = @Content(schema = @Schema(implementation = ApiFailureResponse.class)))
    @PreAuthorize("permitAll()")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("GetCollegeById request received: id={}", id);
        try {
            CollegeResponseDto response = collegeService.getById(id);
            log.info("GetCollegeById success: id={}, slug={}", response.getId(), response.getSlug());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiSuccessResponse<>(response, "College fetched successfully!", 200));
        } catch (NotFoundException e) {
            log.error("GetCollegeById not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 404));
        } catch (CustomException e) {
            log.error("GetCollegeById business error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 400));
        } catch (Exception e) {
            log.error("GetCollegeById internal error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(new ArrayList<>(), e.getMessage(), 500));
        }
    }
}
