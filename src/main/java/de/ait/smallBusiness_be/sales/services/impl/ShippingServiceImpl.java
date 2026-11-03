package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingRepository shippingRepository;
    private final SaleRepository saleRepository;
    private final ModelMapper modelMapper;


    @Override
    public ShippingDto createShipping(NewShippingDto newShippingDto) {

        String name = newShippingDto.getName().trim();

        if (shippingRepository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.SHIPPING_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Shipping shipping = Shipping.builder().name(name).build();
        Shipping savedShipping = shippingRepository.save(shipping);

        return modelMapper.map(savedShipping, ShippingDto.class);
    }


    @Override
    public List<ShippingDto> findAllShipping() {
        return shippingRepository.findAll()
                .stream()
                .map(shipping -> modelMapper.map(shipping, ShippingDto.class))
                .toList();
    }

    @Override
    public ShippingDto updateShipping(Long id, NewShippingDto newShippingDto) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SHIPPING_NOT_FOUND.getDescription()));

        String name = newShippingDto.getName().trim();

        if (!shipping.getName().equals(name) && shippingRepository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.SHIPPING_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        shipping.setName(name);
        Shipping savedShipping = shippingRepository.save(shipping);

        return modelMapper.map(savedShipping, ShippingDto.class);
    }

    @Override
    public ShippingDto getShippingById(Long id) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SHIPPING_NOT_FOUND.getDescription()));
        return modelMapper.map(shipping, ShippingDto.class);
    }


    @Override
    @Transactional
    public void deleteShipping(Long id) {

        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.SHIPPING_NOT_FOUND.getDescription()));

        // Проверка использования в Sale.
        // Имя метода зависит от структуры сущности Sale.
        if (saleRepository.existsByShipping_Id(id)) {
            throw new RestApiException(ErrorDescription.SHIPPING_IN_USE, HttpStatus.CONFLICT);
        }

        shippingRepository.delete(shipping);
        shippingRepository.flush();
    }

}
