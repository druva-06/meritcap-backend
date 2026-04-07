package com.meritcap.controller;

import com.meritcap.DTOs.requestDTOs.documentconfig.CountryRequestDto;
import com.meritcap.DTOs.responseDTOs.documentconfig.CountryResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.response.ApiFailureResponse;
import com.meritcap.response.ApiSuccessResponse;
import com.meritcap.service.CountryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@Slf4j
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public ResponseEntity<?> getAllCountries() {
        try {
            List<CountryResponseDto> countries = countryService.getAllActiveCountries();
            return ResponseEntity.ok(new ApiSuccessResponse<>(countries, "Countries fetched", 200));
        } catch (Exception e) {
            log.error("Error fetching countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllCountriesAdmin() {
        try {
            List<CountryResponseDto> countries = countryService.getAllCountries();
            return ResponseEntity.ok(new ApiSuccessResponse<>(countries, "All countries fetched", 200));
        } catch (Exception e) {
            log.error("Error fetching all countries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCountryById(@PathVariable Long id) {
        try {
            CountryResponseDto country = countryService.getCountryById(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(country, "Country fetched", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error fetching country id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCountry(@RequestBody @Valid CountryRequestDto requestDto) {
        try {
            CountryResponseDto country = countryService.createCountry(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiSuccessResponse<>(country, "Country created", 201));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error creating country", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCountry(@PathVariable Long id,
                                           @RequestBody @Valid CountryRequestDto requestDto) {
        try {
            CountryResponseDto country = countryService.updateCountry(id, requestDto);
            return ResponseEntity.ok(new ApiSuccessResponse<>(country, "Country updated", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (AlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 409));
        } catch (Exception e) {
            log.error("Error updating country id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCountry(@PathVariable Long id) {
        try {
            countryService.deleteCountry(id);
            return ResponseEntity.ok(new ApiSuccessResponse<>(null, "Country deleted", 200));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiFailureResponse<>(null, e.getMessage(), 404));
        } catch (Exception e) {
            log.error("Error deleting country id={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiFailureResponse<>(null, "Server error", 500));
        }
    }
}
