package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.ShippingRepository;
import de.ait.smallBusiness_be.sales.dto.NewShippingDto;
import de.ait.smallBusiness_be.sales.dto.ShippingDto;
import de.ait.smallBusiness_be.sales.models.Shipping;
import de.ait.smallBusiness_be.sales.services.ShippingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository shippingRepository;
    private final ModelMapper modelMapper;

    @Override
    public ShippingDto createShipping(NewShippingDto newShippingDto) {
        if (shippingRepository.existsByName(newShippingDto.getName())) {
            throw new IllegalArgumentException("Shipping with name '" + newShippingDto.getName() + "' already exists");
        }
        Shipping shipping = Shipping.builder()
                .name(newShippingDto.getName())
                .build();
        Shipping savedShipping = shippingRepository.save(shipping);

        return modelMapper.map(savedShipping, ShippingDto.class);
    }

    @Override
    public List<ShippingDto> findAllShipping() {
        List<Shipping> shippings = shippingRepository.findAll();
        if (shippings.isEmpty()) {throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return shippings.stream().map(shipping -> modelMapper.map(shipping, ShippingDto.class)).collect(Collectors.toList());
    }

    @Override
    public ShippingDto updateShipping(Long id, NewShippingDto newShippingDto) {
        Shipping shipping = shippingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Shipping not found"));
        shipping.setName(newShippingDto.getName());
        Shipping savedShipping = shippingRepository.save(shipping);
        return modelMapper.map(savedShipping, ShippingDto.class);
    }

    @Override
    public ShippingDto getShippingById(Long id) {
        Shipping shipping = shippingRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Shipping not found"));
        return modelMapper.map(shipping, ShippingDto.class);
    }

    @Override
    public void deleteShipping(Long id) {
    if (shippingRepository.existsById(id)) {
        throw new EntityNotFoundException("Shipping with id '" + id + "' not found");
    }
    shippingRepository.deleteById(id);
    }
}
