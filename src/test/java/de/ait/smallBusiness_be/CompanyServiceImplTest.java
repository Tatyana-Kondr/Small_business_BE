package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.company.dao.CompanyRepository;
import de.ait.smallBusiness_be.company.dto.CompanyDto;
import de.ait.smallBusiness_be.company.dto.NewCompanyDto;
import de.ait.smallBusiness_be.company.model.Company;
import de.ait.smallBusiness_be.company.service.CompanyServiceImpl;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import static org.assertj.core.api.Assertions.assertThat;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private NewCompanyDto newCompanyDto;
    private Company company;
    private CompanyDto companyDto;

    @BeforeEach
    void setUp() throws IOException {
        newCompanyDto = new NewCompanyDto();
        company = new Company();
        company.setId(1L);
        companyDto = new CompanyDto();
        companyDto.setId(1L);

        // гарантируем, что директория для логотипов создается
        companyService.init();
    }

    @Test
    void createCompany_success() {
        when(modelMapper.map(newCompanyDto, Company.class)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.createCompany(newCompanyDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(companyRepository).save(company);
    }

    @Test
    void getCompany_success() {
        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.getCompany();

        assertEquals(1L, result.getId());
    }

    @Test
    void getCompany_notFound() {
        when(companyRepository.findAll()).thenReturn(List.of());

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> companyService.getCompany()
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains(ErrorDescription.COMPANY_NOT_FOUND.getDescription()));
    }

    @Test
    void updateCompany_success() {
        // given
        Long companyId = 1L;

        Company existingCompany = new Company();
        existingCompany.setId(companyId);
        existingCompany.setName("Old Name");

        NewCompanyDto newCompanyDto = new NewCompanyDto();
        newCompanyDto.setName("New Name");

        Company updatedCompany = new Company();
        updatedCompany.setId(companyId);
        updatedCompany.setName("New Name");

        CompanyDto expectedDto = new CompanyDto();
        expectedDto.setId(companyId);
        expectedDto.setName("New Name");

        // мок репозитория
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(updatedCompany);

        // мок маппера (из DTO -> Entity)
        doAnswer(invocation -> {
            NewCompanyDto dto = invocation.getArgument(0);
            Company target = invocation.getArgument(1);
            target.setName(dto.getName());
            return null; // map(dto, entity) обычно void-like
        }).when(modelMapper).map(any(NewCompanyDto.class), any(Company.class));

        // мок маппера (из Entity -> DTO)
        when(modelMapper.map(any(Company.class), eq(CompanyDto.class))).thenReturn(expectedDto);

        // when
        CompanyDto result = companyService.updateCompany(companyId, newCompanyDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(companyId);
        assertThat(result.getName()).isEqualTo("New Name");

        verify(companyRepository).findById(companyId);
        verify(companyRepository).save(existingCompany);
        verify(modelMapper).map(newCompanyDto, existingCompany);
        verify(modelMapper).map(updatedCompany, CompanyDto.class);
    }


    @Test
    void updateCompany_notFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> companyService.updateCompany(1L, newCompanyDto)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains(ErrorDescription.COMPANY_NOT_FOUND.getDescription()));
    }

    @Test
    void uploadLogo_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("logo.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("fakeImage".getBytes()));

        company.setLogoUrl(null);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.uploadLogo(1L, file);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        // проверяем, что файл реально сохранился
        Path expectedPath = Path.of(System.getProperty("user.dir"), "uploads", "logos", "company_1_logo.png");
        assertTrue(Files.exists(expectedPath));

        // удалим файл после теста
        Files.deleteIfExists(expectedPath);
    }

    @Test
    void uploadLogo_notFound() {
        MultipartFile file = mock(MultipartFile.class);
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> companyService.uploadLogo(1L, file)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains(ErrorDescription.COMPANY_NOT_FOUND.getDescription()));
    }

    @Test
    void uploadLogo_ioError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("logo.png");
        when(file.getInputStream()).thenThrow(new IOException("fail"));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> companyService.uploadLogo(1L, file)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
        assertTrue(exception.getMessage().contains(ErrorDescription.FILE_UPLOAD_FAILED.getDescription()));
    }
}
