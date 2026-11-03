
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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    @TempDir
    Path tempDir;

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

        Path logosDir = tempDir.resolve("logos");

        ReflectionTestUtils.setField(
                companyService,
                "logoDirPath",
                logosDir.toString()
        );

        ReflectionTestUtils.setField(
                companyService,
                "baseUrl",
                "http://localhost:8081"
        );

        companyService.init();
    }

    @Test
    void createCompany_success() {
        when(modelMapper.map(newCompanyDto, Company.class)).thenReturn(company);
        when(companyRepository.save(company)).thenReturn(company);
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.createCompany(newCompanyDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(companyRepository).save(company);
    }

    @Test
    void getCompany_success() {
        when(companyRepository.findAll()).thenReturn(List.of(company));
        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.getCompany();

        assertThat(result.getId()).isEqualTo(1L);

        verify(companyRepository).findAll();
    }

    @Test
    void getCompany_notFound() {
        when(companyRepository.findAll()).thenReturn(List.of());

        RestApiException exception = assertThrows(RestApiException.class, () -> companyService.getCompany());

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(exception.getMessage()).contains(ErrorDescription.COMPANY_NOT_FOUND.getDescription());
    }

    @Test
    void updateCompany_success() {
        Long companyId = 1L;

        company.setName("Old Name");
        newCompanyDto.setName("New Name");

        CompanyDto expectedDto = new CompanyDto();
        expectedDto.setId(companyId);
        expectedDto.setName("New Name");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        doAnswer(invocation -> {
            NewCompanyDto dto = invocation.getArgument(0);
            Company target = invocation.getArgument(1);
            target.setName(dto.getName());
            return null;
        }).when(modelMapper).map(any(NewCompanyDto.class), any(Company.class));

        when(companyRepository.save(company)).thenReturn(company);

        when(modelMapper.map(company, CompanyDto.class)).thenReturn(expectedDto);

        CompanyDto result = companyService.updateCompany(companyId, newCompanyDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(companyId);
        assertThat(result.getName()).isEqualTo("New Name");

        verify(companyRepository).findById(companyId);
        verify(modelMapper).map(newCompanyDto, company);
        verify(companyRepository).save(company);
    }

    @Test
    void updateCompany_notFound() {
        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class, () -> companyService.updateCompany(1L, newCompanyDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void uploadLogo_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("logo.png");

        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("fakeImage".getBytes(StandardCharsets.UTF_8)));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        when(companyRepository.save(company)).thenReturn(company);

        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.uploadLogo(1L, file);

        Path expectedPath = tempDir.resolve("logos/company_1_logo.png");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        assertThat(Files.exists(expectedPath)).isTrue();

        assertThat(company.getLogoUrl()).isEqualTo("/uploads/logos/company_1_logo.png");

        verify(companyRepository).save(company);
    }

    @Test
    void uploadLogo_notFound() {
        MultipartFile file = mock(MultipartFile.class);

        when(companyRepository.findById(1L)).thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class, () -> companyService.uploadLogo(1L, file));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void uploadLogo_ioError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("logo.png");

        when(file.getInputStream()).thenThrow(new IOException("fail"));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        RestApiException exception = assertThrows(
                RestApiException.class, () -> companyService.uploadLogo(1L, file));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        assertThat(exception.getMessage())
                .contains(ErrorDescription.FILE_UPLOAD_FAILED.getDescription());

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    void updateLogo_delegatesToUploadLogo() throws IOException {
        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename()).thenReturn("new-logo.png");

        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("image".getBytes(StandardCharsets.UTF_8)));

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));

        when(companyRepository.save(company)).thenReturn(company);

        when(modelMapper.map(company, CompanyDto.class)).thenReturn(companyDto);

        CompanyDto result = companyService.updateLogo(1L, file);

        assertThat(result.getId()).isEqualTo(1L);

        assertThat(company.getLogoUrl()).isEqualTo("/uploads/logos/company_1_new-logo.png");
    }
}
