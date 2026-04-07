package com.meritcap.service.impl;

import com.meritcap.DTOs.requestDTOs.documentconfig.CountryRequestDto;
import com.meritcap.DTOs.responseDTOs.documentconfig.CountryResponseDto;
import com.meritcap.exception.AlreadyExistException;
import com.meritcap.exception.NotFoundException;
import com.meritcap.model.Country;
import com.meritcap.repository.CountryRepository;
import com.meritcap.service.CountryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    public List<CountryResponseDto> getAllActiveCountries() {
        return countryRepository.findByIsActiveTrueOrderByNameAsc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CountryResponseDto> getAllCountries() {
        return countryRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public CountryResponseDto getCountryById(Long id) {
        return toDto(findById(id));
    }

    @Override
    public CountryResponseDto createCountry(CountryRequestDto requestDto) {
        if (countryRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new AlreadyExistException(Collections.singletonList("Country with name '" + requestDto.getName() + "' already exists"));
        }
        if (countryRepository.existsByCodeIgnoreCase(requestDto.getCode())) {
            throw new AlreadyExistException(Collections.singletonList("Country with code '" + requestDto.getCode() + "' already exists"));
        }

        Country country = Country.builder()
                .name(requestDto.getName())
                .code(requestDto.getCode())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        country = countryRepository.save(country);
        log.info("Created country id={}, name={}", country.getId(), country.getName());
        return toDto(country);
    }

    @Override
    public CountryResponseDto updateCountry(Long id, CountryRequestDto requestDto) {
        Country country = findById(id);

        if (!country.getName().equalsIgnoreCase(requestDto.getName())
                && countryRepository.existsByNameIgnoreCase(requestDto.getName())) {
            throw new AlreadyExistException(Collections.singletonList("Country with name '" + requestDto.getName() + "' already exists"));
        }
        if (!country.getCode().equalsIgnoreCase(requestDto.getCode())
                && countryRepository.existsByCodeIgnoreCase(requestDto.getCode())) {
            throw new AlreadyExistException(Collections.singletonList("Country with code '" + requestDto.getCode() + "' already exists"));
        }

        country.setName(requestDto.getName());
        country.setCode(requestDto.getCode());
        if (requestDto.getIsActive() != null) {
            country.setIsActive(requestDto.getIsActive());
        }

        country = countryRepository.save(country);
        log.info("Updated country id={}", country.getId());
        return toDto(country);
    }

    @Override
    public void deleteCountry(Long id) {
        Country country = findById(id);
        countryRepository.delete(country); // triggers @SQLDelete soft delete
        log.info("Soft-deleted country id={}", id);
    }

    private Country findById(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Country not found with id: " + id));
    }

    private CountryResponseDto toDto(Country country) {
        return CountryResponseDto.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .isActive(country.getIsActive())
                .build();
    }
}
