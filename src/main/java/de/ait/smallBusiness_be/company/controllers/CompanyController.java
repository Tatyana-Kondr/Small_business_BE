package de.ait.smallBusiness_be.company.controllers;

import de.ait.smallBusiness_be.company.controllers.api.CompanyApi;
import de.ait.smallBusiness_be.company.dto.CompanyDto;
import de.ait.smallBusiness_be.company.dto.NewCompanyDto;
import de.ait.smallBusiness_be.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CompanyController implements CompanyApi {

    private final CompanyService companyService;

    @Override
    public CompanyDto createCompany(NewCompanyDto newCompanyDto) {
        return companyService.createCompany(newCompanyDto);
    }

    @Override
    public CompanyDto getCompany() {return companyService.getCompany(); }

    @Override
    public CompanyDto updateCompany(Long id, NewCompanyDto newCompanyDto) {
        return companyService.updateCompany(id, newCompanyDto);
    }

    @Override
    public CompanyDto uploadLogo(Long id, MultipartFile file) {
        return companyService.uploadLogo(id, file);
    }
}
