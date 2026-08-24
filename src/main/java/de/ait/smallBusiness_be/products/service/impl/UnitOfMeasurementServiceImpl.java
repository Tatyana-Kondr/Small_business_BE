package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.UnitOfMeasurementRepository;
import de.ait.smallBusiness_be.products.dto.NewUnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.dto.UnitOfMeasurementDto;
import de.ait.smallBusiness_be.products.model.UnitOfMeasurement;
import de.ait.smallBusiness_be.products.service.UnitOfMeasurementService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UnitOfMeasurementServiceImpl implements UnitOfMeasurementService {

    private final UnitOfMeasurementRepository unitOfMeasurementRepository;
    private final ModelMapper modelMapper;

    @Override
    public UnitOfMeasurementDto createUnitOfMeasurement(NewUnitOfMeasurementDto newUnitOfMeasurementDto) {
        if (unitOfMeasurementRepository.existsByName(newUnitOfMeasurementDto.getName())) {
            throw new IllegalArgumentException("Unit Of Measurement with name '" + newUnitOfMeasurementDto.getName() + "' already exists");
        }
        UnitOfMeasurement unit = UnitOfMeasurement.builder()
                .name(newUnitOfMeasurementDto.getName())
                .build();
        UnitOfMeasurement savedUnit = unitOfMeasurementRepository.save(unit);

        return modelMapper.map(savedUnit, UnitOfMeasurementDto.class);
    }

    @Override
    public List<UnitOfMeasurementDto> findAllUnitsOfMeasurement() {
        List<UnitOfMeasurement> units = unitOfMeasurementRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        if (units.isEmpty()) {throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return units.stream().map(unit -> modelMapper.map(unit, UnitOfMeasurementDto.class)).collect(Collectors.toList());
    }

    @Override
    public UnitOfMeasurementDto updateUnitOfMeasurement(Long id, NewUnitOfMeasurementDto newUnitOfMeasurementDto) {
        UnitOfMeasurement unit = unitOfMeasurementRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Unit Of Measurement not found"));
        unit.setName(newUnitOfMeasurementDto.getName());
        UnitOfMeasurement savedUnit = unitOfMeasurementRepository.save(unit);
        return modelMapper.map(savedUnit, UnitOfMeasurementDto.class);
    }

    @Override
    public UnitOfMeasurementDto getUnitOfMeasurementById(Long id) {
        UnitOfMeasurement unit = unitOfMeasurementRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Unit Of Measurement not found"));
        return modelMapper.map(unit, UnitOfMeasurementDto.class);
    }

    @Override
    public void deleteUnitOfMeasurement(Long id) {
        if (unitOfMeasurementRepository.existsById(id)) {
            throw new EntityNotFoundException("Unit Of Measurement with id '" + id + "' not found");
        }
        unitOfMeasurementRepository.deleteById(id);
    }

}
