
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.TaxRateRepository;
import de.ait.smallBusiness_be.sales.dto.NewTaxRateDto;
import de.ait.smallBusiness_be.sales.dto.TaxRateDto;
import de.ait.smallBusiness_be.sales.models.TaxRate;
import de.ait.smallBusiness_be.sales.services.impl.TaxRateServiceImpl;

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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaxRateServiceImplTest {

    @Mock
    private TaxRateRepository taxRateRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TaxRateServiceImpl service;

    private TaxRate taxRate;
    private NewTaxRateDto newDto;
    private TaxRateDto taxRateDto;

    @BeforeEach
    void setUp() {
        taxRate = TaxRate.builder()
                .id(1L)
                .value(new BigDecimal("19"))
                .active(true)
                .defaultRate(false)
                .build();

        newDto = new NewTaxRateDto();
        newDto.setValue(new BigDecimal("19.00"));
        newDto.setActive(true);
        newDto.setDefaultRate(false);

        taxRateDto = new TaxRateDto();
    }

    @Test
    void createTaxRate_firstRate_becomesDefaultAndActive() {
        newDto.setActive(false);
        newDto.setDefaultRate(false);

        when(taxRateRepository.count()).thenReturn(0L);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TaxRate.class), eq(TaxRateDto.class))).thenReturn(taxRateDto);

        service.createTaxRate(newDto);

        ArgumentCaptor<TaxRate> captor = ArgumentCaptor.forClass(TaxRate.class);

        verify(taxRateRepository).save(captor.capture());

        TaxRate saved = captor.getValue();

        assertThat(saved.getValue()).isEqualByComparingTo("19");
        assertThat(saved.isDefaultRate()).isTrue();
        assertThat(saved.isActive()).isTrue();

        verify(taxRateRepository, never()).findByDefaultRateTrue();
    }

    @Test
    void createTaxRate_nonDefault_success() {
        when(taxRateRepository.count()).thenReturn(1L);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TaxRate.class), eq(TaxRateDto.class))).thenReturn(taxRateDto);

        service.createTaxRate(newDto);

        ArgumentCaptor<TaxRate> captor = ArgumentCaptor.forClass(TaxRate.class);

        verify(taxRateRepository).save(captor.capture());

        assertThat(captor.getValue().isDefaultRate()).isFalse();
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void createTaxRate_inactiveNonDefault_success() {
        newDto.setActive(false);

        when(taxRateRepository.count()).thenReturn(1L);
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TaxRate.class), eq(TaxRateDto.class))).thenReturn(taxRateDto);

        service.createTaxRate(newDto);

        ArgumentCaptor<TaxRate> captor = ArgumentCaptor.forClass(TaxRate.class);

        verify(taxRateRepository).save(captor.capture());

        assertThat(captor.getValue().isActive()).isFalse();
        assertThat(captor.getValue().isDefaultRate()).isFalse();
    }

    @Test
    void createTaxRate_newDefault_clearsPreviousDefault() {
        newDto.setDefaultRate(true);
        newDto.setActive(false);

        TaxRate previousDefault = TaxRate.builder()
                .id(2L)
                .value(new BigDecimal("7"))
                .active(true)
                .defaultRate(true)
                .build();

        when(taxRateRepository.count()).thenReturn(1L);
        when(taxRateRepository.findByDefaultRateTrue()).thenReturn(Optional.of(previousDefault));
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(TaxRate.class), eq(TaxRateDto.class))).thenReturn(taxRateDto);

        service.createTaxRate(newDto);

        assertThat(previousDefault.isDefaultRate()).isFalse();

        verify(taxRateRepository).save(previousDefault);

        ArgumentCaptor<TaxRate> captor = ArgumentCaptor.forClass(TaxRate.class);

        verify(taxRateRepository, times(2)).save(captor.capture());

        TaxRate newRate = captor.getAllValues().get(1);

        assertThat(newRate.isDefaultRate()).isTrue();
        assertThat(newRate.isActive()).isTrue();
    }

    @Test
    void createTaxRate_duplicate_throwsConflict() {
        when(taxRateRepository.existsByValue(new BigDecimal("19"))).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.createTaxRate(newDto));
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void getAllTaxRates_success() {
        when(taxRateRepository.findAllByOrderByValueAsc()).thenReturn(List.of(taxRate));
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);

        List<TaxRateDto> result = service.getAllTaxRates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(taxRateDto);
    }

    @Test
    void getAllTaxRates_empty_returnsEmptyList() {
        when(taxRateRepository.findAllByOrderByValueAsc()).thenReturn(List.of());
        assertThat(service.getAllTaxRates()).isEmpty();
    }

    @Test
    void getActiveTaxRates_success() {
        when(taxRateRepository.findByActiveTrueOrderByValueAsc()).thenReturn(List.of(taxRate));
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);
        List<TaxRateDto> result = service.getActiveTaxRates();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(taxRateDto);
    }

    @Test
    void getActiveTaxRates_empty_returnsEmptyList() {
        when(taxRateRepository.findByActiveTrueOrderByValueAsc()).thenReturn(List.of());
        assertThat(service.getActiveTaxRates()).isEmpty();
    }

    @Test
    void getDefaultTaxRate_success() {
        taxRate.setDefaultRate(true);

        when(taxRateRepository.findByDefaultRateTrue()).thenReturn(Optional.of(taxRate));
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);
        TaxRateDto result = service.getDefaultTaxRate();
        assertThat(result).isSameAs(taxRateDto);
    }

    @Test
    void getDefaultTaxRate_notFound_throws() {
        when(taxRateRepository.findByDefaultRateTrue()).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getDefaultTaxRate());
    }

    @Test
    void updateTaxRate_success() {
        newDto.setValue(new BigDecimal("20.00"));

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxRateRepository.save(taxRate)).thenReturn(taxRate);
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);

        service.updateTaxRate(1L, newDto);

        assertThat(taxRate.getValue()).isEqualByComparingTo("20");
        assertThat(taxRate.isActive()).isTrue();
        assertThat(taxRate.isDefaultRate()).isFalse();
        verify(taxRateRepository).existsByValue(new BigDecimal("2E+1"));
        verify(taxRateRepository).save(taxRate);
    }

    @Test
    void updateTaxRate_sameValue_skipsDuplicateCheck() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxRateRepository.save(taxRate)).thenReturn(taxRate);
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);

        service.updateTaxRate(1L, newDto);

        verify(taxRateRepository, never()).existsByValue(any(BigDecimal.class));
        verify(taxRateRepository).save(taxRate);
    }

    @Test
    void updateTaxRate_duplicate_throwsConflict() {
        newDto.setValue(new BigDecimal("7"));

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxRateRepository.existsByValue(new BigDecimal("7"))).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateTaxRate(1L, newDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void updateTaxRate_defaultCannotBeUnset() {
        taxRate.setDefaultRate(true);

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateTaxRate(1L, newDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void updateTaxRate_setNewDefault_clearsPrevious() {
        newDto.setDefaultRate(true);
        newDto.setActive(false);

        TaxRate previousDefault = TaxRate.builder()
                .id(2L)
                .value(new BigDecimal("7"))
                .active(true)
                .defaultRate(true)
                .build();

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        when(taxRateRepository.findByDefaultRateTrue()).thenReturn(Optional.of(previousDefault));
        when(taxRateRepository.save(any(TaxRate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(taxRate, TaxRateDto.class)).thenReturn(taxRateDto);

        service.updateTaxRate(1L, newDto);

        assertThat(previousDefault.isDefaultRate()).isFalse();
        assertThat(taxRate.isDefaultRate()).isTrue();
        assertThat(taxRate.isActive()).isTrue();

        verify(taxRateRepository).save(previousDefault);
        verify(taxRateRepository).save(taxRate);
    }

    @Test
    void updateTaxRate_notFound_throws() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.updateTaxRate(1L, newDto));
        verify(taxRateRepository, never()).save(any(TaxRate.class));
    }

    @Test
    void deleteTaxRate_success() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));
        service.deleteTaxRate(1L);
        verify(taxRateRepository).delete(taxRate);
    }

    @Test
    void deleteTaxRate_default_throwsConflict() {
        taxRate.setDefaultRate(true);

        when(taxRateRepository.findById(1L)).thenReturn(Optional.of(taxRate));

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteTaxRate(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(taxRateRepository, never()).delete(any(TaxRate.class));
    }

    @Test
    void deleteTaxRate_notFound_throws() {
        when(taxRateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteTaxRate(1L));
        verify(taxRateRepository, never())
                .delete(any(TaxRate.class));
    }
}

