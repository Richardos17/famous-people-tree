package com.richardos17.family_tree.service;

import com.richardos17.family_tree.DTOs.CountryDTO;
import com.richardos17.family_tree.domain.Country;
import com.richardos17.family_tree.repository.CountryRepository;
import com.richardos17.family_tree.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class CountryCreateService {
    private final CountryRepository countryRepository;
    private final IdGenerator idGenerator;

    public Country createCountry(CountryDTO countryDTO) {
        Country country = Country.builder()
                .name(countryDTO.getName())
                .localId(idGenerator.generateUniqueCountryId())
                .build();

        return countryRepository.save(country);
    }
}
