package com.meritcap.service;

import com.meritcap.DTOs.requestDTOs.documentconfig.CountryRequestDto;
import com.meritcap.DTOs.responseDTOs.documentconfig.CountryResponseDto;

import java.util.List;

public interface CountryService {
    List<CountryResponseDto> getAllActiveCountries();
    List<CountryResponseDto> getAllCountries();
    CountryResponseDto getCountryById(Long id);
    CountryResponseDto createCountry(CountryRequestDto requestDto);
    CountryResponseDto updateCountry(Long id, CountryRequestDto requestDto);
    void deleteCountry(Long id);
}
