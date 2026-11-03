package de.ait.smallBusiness_be.products.service.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.products.dao.ProductRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnitOfMeasurementServiceImpl implements UnitOfMeasurementService {

    private final UnitOfMeasurementRepository unitOfMeasurementRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UnitOfMeasurementDto createUnitOfMeasurement(NewUnitOfMeasurementDto dto) {

        String name = normalizeName(dto.getName());

        if (unitOfMeasurementRepository.existsByNameIgnoreCase(name)) {
            throw new RestApiException(ErrorDescription.UNIT_OF_MEASUREMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        UnitOfMeasurement unit = UnitOfMeasurement.builder()
                .name(name)
                .build();

        UnitOfMeasurement savedUnit = unitOfMeasurementRepository.save(unit);

        return modelMapper.map(savedUnit, UnitOfMeasurementDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitOfMeasurementDto> findAllUnitsOfMeasurement() {

        return unitOfMeasurementRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(unit -> modelMapper.map(unit, UnitOfMeasurementDto.class))
                .toList();
    }

    @Override
    @Transactional
    public UnitOfMeasurementDto updateUnitOfMeasurement(Long id, NewUnitOfMeasurementDto dto) {

        UnitOfMeasurement unit = getUnitOfMeasurementOrThrow(id);

        String name = normalizeName(dto.getName());

        if (unitOfMeasurementRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {

            throw new RestApiException(ErrorDescription.UNIT_OF_MEASUREMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        unit.setName(name);

        UnitOfMeasurement savedUnit = unitOfMeasurementRepository.save(unit);

        return modelMapper.map(savedUnit, UnitOfMeasurementDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasurementDto getUnitOfMeasurementById(Long id) {

        UnitOfMeasurement unit = getUnitOfMeasurementOrThrow(id);

        return modelMapper.map(unit, UnitOfMeasurementDto.class);
    }

    @Override
    @Transactional
    public void deleteUnitOfMeasurement(Long id) {

        UnitOfMeasurement unit = getUnitOfMeasurementOrThrow(id);

        if (productRepository.existsByUnitOfMeasurement_Id(id)) {
            throw new RestApiException(ErrorDescription.UNIT_OF_MEASUREMENT_IN_USE, HttpStatus.CONFLICT);
        }

        unitOfMeasurementRepository.delete(unit);
        unitOfMeasurementRepository.flush();
    }

    @Override
    @Transactional(readOnly = true)
    public UnitOfMeasurement getUnitOfMeasurementOrThrow(Long id) {

        return unitOfMeasurementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.UNIT_OF_MEASUREMENT_NOT_FOUND.getDescription()));
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }
}