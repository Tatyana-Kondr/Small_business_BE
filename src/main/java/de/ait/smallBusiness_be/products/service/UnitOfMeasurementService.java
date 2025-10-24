package de.ait.smallBusiness_be.products.service;

import de.ait.smallBusiness_be.products.dto.NewUnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.dto.UnitOfMeasurementDto;

import java.util.List;

public interface UnitOfMeasurementService {
    UnitOfMeasurementDto createUnitOfMeasurement(NewUnitOfMeasurementDto newUnitOfMeasurementDto);
    List<UnitOfMeasurementDto> findAllUnitsOfMeasurement();
    UnitOfMeasurementDto updateUnitOfMeasurement(Long id, NewUnitOfMeasurementDto newUnitOfMeasurementDto);
    UnitOfMeasurementDto getUnitOfMeasurementById(Long id);
    void deleteUnitOfMeasurement(Long id);
}
