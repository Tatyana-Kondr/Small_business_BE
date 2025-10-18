package de.ait.smallBusiness_be.sales.services;

import de.ait.smallBusiness_be.sales.dto.NewShippingDto;
import de.ait.smallBusiness_be.sales.dto.ShippingDto;

import java.util.List;

public interface ShippingService {
    ShippingDto createShipping(NewShippingDto newShippingDto);
    List<ShippingDto> findAllShipping();
    ShippingDto updateShipping(Long id, NewShippingDto newShippingDto);
    ShippingDto getShippingById(Long id);
    void deleteShipping(Long id);
}
