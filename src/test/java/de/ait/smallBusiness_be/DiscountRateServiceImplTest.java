
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.DiscountRateRepository;
import de.ait.smallBusiness_be.sales.dto.DiscountRateDto;
import de.ait.smallBusiness_be.sales.dto.NewDiscountRateDto;
import de.ait.smallBusiness_be.sales.models.DiscountRate;
import de.ait.smallBusiness_be.sales.services.impl.DiscountRateServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountRateServiceImplTest {

    @Mock
    private DiscountRateRepository discountRateRepository;

    @InjectMocks
    private DiscountRateServiceImpl service;

    private DiscountRate discountRate;
    private NewDiscountRateDto newDto;

    @BeforeEach
    void setUp() {
        discountRate = DiscountRate.builder()
                .id(1L)
                .value(new BigDecimal("10.00"))
                .active(true)
                .build();

        newDto = new NewDiscountRateDto();
        newDto.setValue(new BigDecimal("10.00"));
        newDto.setActive(true);
    }

    @Test
    void createDiscountRate_success() {
        when(discountRateRepository.save(any(DiscountRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountRateDto result = service.createDiscountRate(newDto);

        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualByComparingTo("10.00");
        assertThat(result.isActive()).isTrue();

        verify(discountRateRepository).existsByValue(new BigDecimal("10.00"));
        verify(discountRateRepository).save(any(DiscountRate.class));
    }

    @Test
    void createDiscountRate_inactive_success() {
        newDto.setActive(false);

        when(discountRateRepository.save(any(DiscountRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountRateDto result = service.createDiscountRate(newDto);

        assertThat(result.isActive()).isFalse();
        assertThat(result.getValue()).isEqualByComparingTo("10.00");
    }

    @Test
    void createDiscountRate_zero_alwaysActive() {
        newDto.setValue(BigDecimal.ZERO);
        newDto.setActive(false);

        when(discountRateRepository.save(any(DiscountRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DiscountRateDto result = service.createDiscountRate(newDto);

        assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void createDiscountRate_duplicate_throwsConflict() {
        when(discountRateRepository.existsByValue(new BigDecimal("10.00"))).thenReturn(true);
        RestApiException exception = assertThrows(RestApiException.class, () -> service.createDiscountRate(newDto));
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(discountRateRepository, never()).save(any(DiscountRate.class));
    }

    @Test
    void createDiscountRate_nullValue_throws() {
        newDto.setValue(null);
        assertThrows(NullPointerException.class, () -> service.createDiscountRate(newDto));
        verify(discountRateRepository, never()).save(any(DiscountRate.class));
    }

    @Test
    void getAllDiscountRates_success() {
        DiscountRate secondRate = DiscountRate.builder()
                .id(2L)
                .value(new BigDecimal("20.00"))
                .active(false)
                .build();

        when(discountRateRepository.findAllByOrderByValueAsc()).thenReturn(List.of(discountRate, secondRate));

        List<DiscountRateDto> result = service.getAllDiscountRates();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualByComparingTo("10.00");
        assertThat(result.get(1).getValue()).isEqualByComparingTo("20.00");
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(1).isActive()).isFalse();
        verify(discountRateRepository).findAllByOrderByValueAsc();
    }

    @Test
    void getAllDiscountRates_empty_returnsEmptyList() {
        when(discountRateRepository.findAllByOrderByValueAsc()).thenReturn(List.of());
        List<DiscountRateDto> result = service.getAllDiscountRates();
        assertThat(result).isEmpty();
    }

    @Test
    void getActiveDiscountRates_success() {
        when(discountRateRepository.findByActiveTrueOrderByValueAsc()).thenReturn(List.of(discountRate));

        List<DiscountRateDto> result = service.getActiveDiscountRates();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
        assertThat(result.get(0).getValue()).isEqualByComparingTo("10.00");
        verify(discountRateRepository).findByActiveTrueOrderByValueAsc();
    }

    @Test
    void getActiveDiscountRates_empty_returnsEmptyList() {
        when(discountRateRepository.findByActiveTrueOrderByValueAsc()).thenReturn(List.of());
        List<DiscountRateDto> result = service.getActiveDiscountRates();
        assertThat(result).isEmpty();
    }

    @Test
    void updateDiscountRate_success() {
        NewDiscountRateDto updateDto = new NewDiscountRateDto();

        updateDto.setValue(new BigDecimal("15.00"));
        updateDto.setActive(true);

        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        when(discountRateRepository.save(discountRate)).thenReturn(discountRate);

        DiscountRateDto result = service.updateDiscountRate(1L, updateDto);

        assertThat(result.getValue()).isEqualByComparingTo("15.00");
        assertThat(result.isActive()).isTrue();
        verify(discountRateRepository).existsByValue(new BigDecimal("15.00"));
        verify(discountRateRepository).save(discountRate);
    }

    @Test
    void updateDiscountRate_sameValue_success() {
        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        when(discountRateRepository.save(discountRate)).thenReturn(discountRate);

        DiscountRateDto result = service.updateDiscountRate(1L, newDto);

        assertThat(result.getValue()).isEqualByComparingTo("10.00");
        verify(discountRateRepository, never()).existsByValue(any(BigDecimal.class));
        verify(discountRateRepository).save(discountRate);
    }

    @Test
    void updateDiscountRate_changeActiveStatus_success() {
        newDto.setActive(false);

        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        when(discountRateRepository.save(discountRate)).thenReturn(discountRate);

        DiscountRateDto result = service.updateDiscountRate(1L, newDto);

        assertThat(result.isActive()).isFalse();
        assertThat(discountRate.isActive()).isFalse();
        verify(discountRateRepository).save(discountRate);
    }

    @Test
    void updateDiscountRate_duplicate_throwsConflict() {
        NewDiscountRateDto updateDto = new NewDiscountRateDto();

        updateDto.setValue(new BigDecimal("20.00"));
        updateDto.setActive(true);

        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        when(discountRateRepository.existsByValue(new BigDecimal("20.00"))).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateDiscountRate(1L, updateDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(discountRate.getValue()).isEqualByComparingTo("10.00");
        verify(discountRateRepository, never()).save(any(DiscountRate.class));
    }

    @Test
    void updateDiscountRate_zeroRate_throwsConflict() {
        DiscountRate zeroRate = DiscountRate.builder()
                .id(2L)
                .value(BigDecimal.ZERO)
                .active(true)
                .build();

        when(discountRateRepository.findById(2L)).thenReturn(Optional.of(zeroRate));

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateDiscountRate(2L, newDto));
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(discountRateRepository, never()).save(any(DiscountRate.class));
    }

    @Test
    void updateDiscountRate_notFound_throws() {
        when(discountRateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.updateDiscountRate(1L, newDto));
        verify(discountRateRepository, never()).save(any(DiscountRate.class));
    }

    @Test
    void updateDiscountRate_toZero_alwaysActive() {
        NewDiscountRateDto updateDto = new NewDiscountRateDto();

        updateDto.setValue(BigDecimal.ZERO);
        updateDto.setActive(false);

        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        when(discountRateRepository.save(discountRate)).thenReturn(discountRate);

        DiscountRateDto result = service.updateDiscountRate(1L, updateDto);

        assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void deleteDiscountRate_success() {
        when(discountRateRepository.findById(1L)).thenReturn(Optional.of(discountRate));
        service.deleteDiscountRate(1L);
        verify(discountRateRepository).delete(discountRate);
    }

    @Test
    void deleteDiscountRate_zeroRate_throwsConflict() {
        DiscountRate zeroRate = DiscountRate.builder()
                .id(2L)
                .value(BigDecimal.ZERO)
                .active(true)
                .build();

        when(discountRateRepository.findById(2L)).thenReturn(Optional.of(zeroRate));

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteDiscountRate(2L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(discountRateRepository, never()).delete(any(DiscountRate.class));
    }

    @Test
    void deleteDiscountRate_notFound_throws() {
        when(discountRateRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteDiscountRate(1L));
        verify(discountRateRepository, never()).delete(any(DiscountRate.class));
    }
}

