package de.ait.smallBusiness_be.company.service;

import de.ait.smallBusiness_be.company.dto.CompanyDto;
import de.ait.smallBusiness_be.company.dto.NewCompanyDto;
import org.springframework.web.multipart.MultipartFile;

public interface CompanyService {

    CompanyDto createCompany(NewCompanyDto newCompanyDto);
    CompanyDto getCompany();
    CompanyDto updateCompany(Long id, NewCompanyDto newCompanyDto);
    CompanyDto uploadLogo(Long id, MultipartFile file);
    CompanyDto updateLogo(Long id, MultipartFile file);
}
