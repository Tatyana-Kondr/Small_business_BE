
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
import de.ait.smallBusiness_be.products.dao.UnitOfMeasurementRepository;
import de.ait.smallBusiness_be.products.dto.NewUnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.dto.UnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import de.ait.smallBusiness_be.products.service.impl.UnitOfMeasurementServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitOfMeasurementServiceImplTest {

    @Mock
    private UnitOfMeasurementRepository unitOfMeasurementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UnitOfMeasurementServiceImpl service;

    private UnitOfMeasurement unit;
    private UnitOfMeasurementDto unitDto;
    private NewUnitOfMeasurementDto newDto;

    @BeforeEach
    void setUp() {
        unit = UnitOfMeasurement.builder()
                .name("Stück")
                .build();

        unitDto = new UnitOfMeasurementDto();
        newDto = new NewUnitOfMeasurementDto();
        newDto.setName("Stück");
    }

    @Test
    void createUnitOfMeasurement_success() {
        when(unitOfMeasurementRepository.save(any(UnitOfMeasurement.class))).thenReturn(unit);
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        UnitOfMeasurementDto result = service.createUnitOfMeasurement(newDto);

        assertThat(result).isSameAs(unitDto);

        verify(unitOfMeasurementRepository).existsByNameIgnoreCase("Stück");
        verify(unitOfMeasurementRepository).save(any(UnitOfMeasurement.class));
    }

    @Test
    void createUnitOfMeasurement_trimsName() {
        newDto.setName("  Stück  ");

        when(unitOfMeasurementRepository.save(any(UnitOfMeasurement.class))).thenReturn(unit);
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        service.createUnitOfMeasurement(newDto);

        ArgumentCaptor<UnitOfMeasurement> captor = ArgumentCaptor.forClass(UnitOfMeasurement.class);

        verify(unitOfMeasurementRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEqualTo("Stück");

        verify(unitOfMeasurementRepository).existsByNameIgnoreCase("Stück");
    }

    @Test
    void createUnitOfMeasurement_duplicate_throws() {
        when(unitOfMeasurementRepository.existsByNameIgnoreCase("Stück")).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.createUnitOfMeasurement(newDto));

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(unitOfMeasurementRepository, never()).save(any(UnitOfMeasurement.class));
    }

    @Test
    void createUnitOfMeasurement_nullName_normalizesToEmpty() {
        newDto.setName(null);

        when(unitOfMeasurementRepository
                .save(any(UnitOfMeasurement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(UnitOfMeasurement.class), eq(UnitOfMeasurementDto.class))).thenReturn(unitDto);

        service.createUnitOfMeasurement(newDto);

        verify(unitOfMeasurementRepository).existsByNameIgnoreCase("");

        ArgumentCaptor<UnitOfMeasurement> captor = ArgumentCaptor.forClass(UnitOfMeasurement.class);

        verify(unitOfMeasurementRepository).save(captor.capture());

        assertThat(captor.getValue().getName()).isEmpty();
    }

    @Test
    void findAllUnitsOfMeasurement_success() {
        when(unitOfMeasurementRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of(unit));
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        List<UnitOfMeasurementDto> result = service.findAllUnitsOfMeasurement();

        assertThat(result).hasSize(1).containsExactly(unitDto);

        verify(unitOfMeasurementRepository).findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Test
    void findAllUnitsOfMeasurement_empty_returnsEmptyList() {
        when(unitOfMeasurementRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))).thenReturn(List.of());

        List<UnitOfMeasurementDto> result = service.findAllUnitsOfMeasurement();

        assertThat(result).isEmpty();

        verifyNoInteractions(modelMapper);
    }

    @Test
    void getUnitOfMeasurementById_success() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        UnitOfMeasurementDto result = service.getUnitOfMeasurementById(1L);

        assertThat(result).isSameAs(unitDto);

        verify(unitOfMeasurementRepository).findById(1L);
    }

    @Test
    void getUnitOfMeasurementById_notFound_throws() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getUnitOfMeasurementById(1L)).isInstanceOf(EntityNotFoundException.class);
        verifyNoInteractions(modelMapper);
    }

    @Test
    void updateUnitOfMeasurement_success() {
        NewUnitOfMeasurementDto updateDto = new NewUnitOfMeasurementDto();
        updateDto.setName("Kilogramm");

        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitOfMeasurementRepository.save(unit)).thenReturn(unit);
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        UnitOfMeasurementDto result = service.updateUnitOfMeasurement(1L, updateDto);

        assertThat(result).isSameAs(unitDto);
        assertThat(unit.getName()).isEqualTo("Kilogramm");

        verify(unitOfMeasurementRepository).existsByNameIgnoreCaseAndIdNot("Kilogramm", 1L);
        verify(unitOfMeasurementRepository).save(unit);
    }

    @Test
    void updateUnitOfMeasurement_trimsName() {
        NewUnitOfMeasurementDto updateDto = new NewUnitOfMeasurementDto();
        updateDto.setName("  Kilogramm  ");

        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitOfMeasurementRepository.save(unit)).thenReturn(unit);
        when(modelMapper.map(unit, UnitOfMeasurementDto.class)).thenReturn(unitDto);

        service.updateUnitOfMeasurement(1L, updateDto);

        assertThat(unit.getName()).isEqualTo("Kilogramm");

        verify(unitOfMeasurementRepository).existsByNameIgnoreCaseAndIdNot("Kilogramm", 1L);
    }

    @Test
    void updateUnitOfMeasurement_duplicate_throws() {
        NewUnitOfMeasurementDto updateDto = new NewUnitOfMeasurementDto();
        updateDto.setName("Kilogramm");

        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(unitOfMeasurementRepository.existsByNameIgnoreCaseAndIdNot("Kilogramm", 1L)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateUnitOfMeasurement(1L, updateDto));

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(unit.getName()).isEqualTo("Stück");

        verify(unitOfMeasurementRepository, never()).save(any(UnitOfMeasurement.class));
    }

    @Test
    void updateUnitOfMeasurement_notFound_throws() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> service.updateUnitOfMeasurement(1L, newDto)).isInstanceOf(EntityNotFoundException.class);

        verify(unitOfMeasurementRepository, never()).save(any(UnitOfMeasurement.class));
    }

    @Test
    void deleteUnitOfMeasurement_success() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));

        service.deleteUnitOfMeasurement(1L);

        verify(productRepository).existsByUnitOfMeasurement_Id(1L);
        verify(unitOfMeasurementRepository).delete(unit);
        verify(unitOfMeasurementRepository).flush();
    }

    @Test
    void deleteUnitOfMeasurement_inUse_throwsConflict() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(productRepository.existsByUnitOfMeasurement_Id(1L)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteUnitOfMeasurement(1L));

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(unitOfMeasurementRepository, never()).delete(any(UnitOfMeasurement.class));
        verify(unitOfMeasurementRepository, never()).flush();
    }

    @Test
    void deleteUnitOfMeasurement_notFound_throws() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteUnitOfMeasurement(1L)).isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(productRepository);
        verify(unitOfMeasurementRepository, never()).delete(any(UnitOfMeasurement.class));
    }

    @Test
    void getUnitOfMeasurementOrThrow_success() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.of(unit));
        UnitOfMeasurement result = service.getUnitOfMeasurementOrThrow(1L);
        assertThat(result).isSameAs(unit);
        verify(unitOfMeasurementRepository).findById(1L);
    }

    @Test
    void getUnitOfMeasurementOrThrow_notFound_throws() {
        when(unitOfMeasurementRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(
                () -> service.getUnitOfMeasurementOrThrow(1L)).isInstanceOf(EntityNotFoundException.class);
    }
}
