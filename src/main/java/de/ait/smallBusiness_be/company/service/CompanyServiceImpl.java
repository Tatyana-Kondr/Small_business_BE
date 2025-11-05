package de.ait.smallBusiness_be.company.service;

import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.dto.CompanyDto;
import de.ait.smallBusiness_be.company.dto.NewCompanyDto;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final ModelMapper modelMapper;

    // Метод определяет путь к каталогу, где лежит jar
    private static String getJarDir() {
        try {
            return Paths.get(CompanyServiceImpl.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI())
                    .getParent()
                    .toString();
        } catch (Exception e) {
            return System.getProperty("user.dir");
        }
    }

    // Путь к папке с логотипами
    private final Path logoDir = Paths.get(getJarDir(), "uploads", "logos");

    @PostConstruct
    public void init() throws IOException {
        if (!Files.exists(logoDir)) {
            Files.createDirectories(logoDir);
        }
    }

    @Override
    public CompanyDto createCompany(NewCompanyDto newCompanyDto) {
        Company company = modelMapper.map(newCompanyDto, Company.class);
        Company savedCompany = companyRepository.save(company);
        return modelMapper.map(savedCompany, CompanyDto.class);
    }

    @Override
    public CompanyDto getCompany() {
        Company company = companyRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));

        return modelMapper.map(company, CompanyDto.class);
    }

    @Override
    public CompanyDto updateCompany(Long id, NewCompanyDto newCompanyDto) {
        Company company = companyRepository.findById(id).orElseThrow(()->
                new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));
        modelMapper.map(newCompanyDto, company);
        Company updatedCompany = companyRepository.save(company);
        return modelMapper.map(updatedCompany, CompanyDto.class);
    }

    @Override
    public CompanyDto uploadLogo(Long id, MultipartFile file) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.COMPANY_NOT_FOUND, HttpStatus.NOT_FOUND));

        try {
            // Удаляем старый логотип, если он есть
            if (company.getLogoUrl() != null && !company.getLogoUrl().isBlank()) {
                Path oldFile = Paths.get(System.getProperty("user.dir"), company.getLogoUrl().replace("/uploads", "uploads"));
                Files.deleteIfExists(oldFile);
            }

            String filename = "company_" + id + "_" + file.getOriginalFilename();
            Path filePath = logoDir.resolve(filename).normalize().toAbsolutePath();

            // Сохраняем файл
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Обновляем company
            company.setLogoUrl("/uploads/logos/" + filename);
            Company saved = companyRepository.save(company);

            return modelMapper.map(saved, CompanyDto.class);

        } catch (IOException e) {
            throw new RestApiException(ErrorDescription.FILE_UPLOAD_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Метод для обновления логотипа (можно вызвать отдельно)
    @Override
    public CompanyDto updateLogo(Long id, MultipartFile file) {
        return uploadLogo(id, file);
    }
}
