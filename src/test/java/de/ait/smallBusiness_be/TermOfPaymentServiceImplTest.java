
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.TermOfPaymentRepository;
import de.ait.smallBusiness_be.sales.dto.NewTermOfPaymentDto;
import de.ait.smallBusiness_be.sales.dto.TermOfPaymentDto;
import de.ait.smallBusiness_be.sales.models.TermOfPayment;
import de.ait.smallBusiness_be.sales.services.impl.TermOfPaymentServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermOfPaymentServiceImplTest {

    @Mock
    private TermOfPaymentRepository repository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TermOfPaymentServiceImpl service;

    private TermOfPayment term;
    private TermOfPaymentDto termDto;
    private NewTermOfPaymentDto newDto;

    @BeforeEach
    void setUp() {
        term = TermOfPayment.builder()
                .name("Vorkasse")
                .build();

        termDto = new TermOfPaymentDto();
        newDto = new NewTermOfPaymentDto();
        newDto.setName("Vorkasse");
    }

    @Test
    void createTermOfPayment_success() {
        when(repository.save(any(TermOfPayment.class))).thenReturn(term);
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        TermOfPaymentDto result = service.createTermOfPayment(newDto);

        assertThat(result).isSameAs(termDto);

        verify(repository).existsByName("Vorkasse");
        verify(repository).save(any(TermOfPayment.class));
    }

    @Test
    void createTermOfPayment_trimsName() {
        newDto.setName("  Vorkasse  ");

        when(repository.save(any(TermOfPayment.class))).thenReturn(term);
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        service.createTermOfPayment(newDto);

        ArgumentCaptor<TermOfPayment> captor = ArgumentCaptor.forClass(TermOfPayment.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Vorkasse");
        verify(repository).existsByName("Vorkasse");
    }

    @Test
    void createTermOfPayment_duplicate_throwsConflict() {
        when(repository.existsByName("Vorkasse")).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.createTermOfPayment(newDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(repository, never()).save(any(TermOfPayment.class));
        verifyNoInteractions(modelMapper);
    }

    @Test
    void createTermOfPayment_nullName_throws() {
        newDto.setName(null);
        assertThrows(NullPointerException.class, () -> service.createTermOfPayment(newDto));
        verifyNoInteractions(repository, modelMapper);
    }

    @Test
    void createTermOfPayment_blankName_isTrimmedToEmpty() {
        newDto.setName("   ");

        when(repository.save(any(TermOfPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TermOfPayment.class), eq(TermOfPaymentDto.class))).thenReturn(termDto);

        service.createTermOfPayment(newDto);

        ArgumentCaptor<TermOfPayment> captor = ArgumentCaptor.forClass(TermOfPayment.class);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEmpty();
        verify(repository).existsByName("");
    }

    @Test
    void findAllTermsOfPayment_success() {
        TermOfPayment secondTerm = TermOfPayment.builder()
                .name("14 Tage netto")
                .build();

        TermOfPaymentDto secondDto = new TermOfPaymentDto();

        when(repository.findAll()).thenReturn(List.of(term, secondTerm));
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);
        when(modelMapper.map(secondTerm, TermOfPaymentDto.class)).thenReturn(secondDto);

        List<TermOfPaymentDto> result = service.findAllTermsOfPayment();

        assertThat(result).containsExactly(termDto, secondDto);
        verify(repository).findAll();
        verify(modelMapper, times(2)).map(any(TermOfPayment.class), eq(TermOfPaymentDto.class));
    }

    @Test
    void findAllTermsOfPayment_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<TermOfPaymentDto> result = service.findAllTermsOfPayment();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    void findTermOfPaymentById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        TermOfPaymentDto result = service.findTermOfPaymentById(1L);

        assertThat(result).isSameAs(termDto);
        verify(repository).findById(1L);
    }

    @Test
    void findTermOfPaymentById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.findTermOfPaymentById(1L));
        assertThat(exception.getMessage()).isEqualTo(ErrorDescription.TERM_OF_PAYMENT_NOT_FOUND.getDescription());
        verifyNoInteractions(modelMapper);
    }

    @Test
    void updateTermOfPayment_success() {
        NewTermOfPaymentDto updateDto = new NewTermOfPaymentDto();
        updateDto.setName("14 Tage netto");

        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(repository.save(term)).thenReturn(term);
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        TermOfPaymentDto result = service.updateTermOfPayment(1L, updateDto);

        assertThat(result).isSameAs(termDto);
        assertThat(term.getName()).isEqualTo("14 Tage netto");

        verify(repository).existsByName("14 Tage netto");
        verify(repository).save(term);
    }

    @Test
    void updateTermOfPayment_sameName_skipsDuplicateCheck() {
        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(repository.save(term)).thenReturn(term);
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        TermOfPaymentDto result = service.updateTermOfPayment(1L, newDto);

        assertThat(result).isSameAs(termDto);
        verify(repository, never()).existsByName(anyString());
        verify(repository).save(term);
    }

    @Test
    void updateTermOfPayment_trimsName() {
        NewTermOfPaymentDto updateDto = new NewTermOfPaymentDto();
        updateDto.setName("  30 Tage netto  ");

        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(repository.save(term)).thenReturn(term);
        when(modelMapper.map(term, TermOfPaymentDto.class)).thenReturn(termDto);

        service.updateTermOfPayment(1L, updateDto);

        assertThat(term.getName()).isEqualTo("30 Tage netto");
        verify(repository).existsByName("30 Tage netto");
    }

    @Test
    void updateTermOfPayment_duplicate_throwsConflict() {
        NewTermOfPaymentDto updateDto = new NewTermOfPaymentDto();
        updateDto.setName("14 Tage netto");

        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(repository.existsByName("14 Tage netto")).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateTermOfPayment(1L, updateDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(term.getName()).isEqualTo("Vorkasse");
        verify(repository, never()).save(any(TermOfPayment.class));
    }

    @Test
    void updateTermOfPayment_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.updateTermOfPayment(1L, newDto));
        verify(repository, never()).save(any(TermOfPayment.class));
    }

    @Test
    void deleteTermOfPayment_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(term));

        service.deleteTermOfPayment(1L);

        verify(saleRepository).existsByTermsOfPayment_Id(1L);
        verify(repository).delete(term);
        verify(repository).flush();
    }

    @Test
    void deleteTermOfPayment_inUse_throwsConflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(term));
        when(saleRepository.existsByTermsOfPayment_Id(1L)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteTermOfPayment(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(repository, never()).delete(any(TermOfPayment.class));
        verify(repository, never()).flush();
    }

    @Test
    void deleteTermOfPayment_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteTermOfPayment(1L));
        verifyNoInteractions(saleRepository);
        verify(repository, never()).delete(any(TermOfPayment.class));
    }

    @Test
    void getTermOfPaymentOrThrow_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(term));
        TermOfPayment result = service.getTermOfPaymentOrThrow(1L);
        assertThat(result).isSameAs(term);
        verify(repository).findById(1L);
    }

    @Test
    void getTermOfPaymentOrThrow_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> service.getTermOfPaymentOrThrow(1L));
        assertThat(exception.getMessage()).isEqualTo(ErrorDescription.TERM_OF_PAYMENT_NOT_FOUND.getDescription());
    }
}

