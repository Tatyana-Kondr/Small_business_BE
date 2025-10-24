package de.ait.smallBusiness_be.products.controller;

import de.ait.smallBusiness_be.products.controller.api.UnitOfMeasurementApi;
import de.ait.smallBusiness_be.products.dao.UnitOfMeasurementRepository;
import de.ait.smallBusiness_be.products.dto.NewUnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.dto.UnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.service.UnitOfMeasurementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UnitOfMeasurementController implements UnitOfMeasurementApi {

    private final UnitOfMeasurementService unitOfMeasurementService
            ;
    @Override
    public UnitOfMeasurementDto addUnitOfMeasurement(NewUnitOfMeasurementDto newUnit) {
        return unitOfMeasurementService.createUnitOfMeasurement(newUnit);
    }

    @Override
    public List<UnitOfMeasurementDto> getAllUnitOfMeasurements() {
        return unitOfMeasurementService.findAllUnitsOfMeasurement();
    }

    @Override
    public UnitOfMeasurementDto getUnitOfMeasurementById(Long id) {
        return unitOfMeasurementService.getUnitOfMeasurementById(id);
    }

    @Override
    public UnitOfMeasurementDto updateUnitOfMeasurement(Long id, NewUnitOfMeasurementDto newUnitOfMeasurementDto) {
        return unitOfMeasurementService.updateUnitOfMeasurement(id, newUnitOfMeasurementDto);
    }

    @Override
    public void deleteUnitOfMeasurement(Long id) {
    unitOfMeasurementService.deleteUnitOfMeasurement(id);
    }
}
