package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.ShippingApi;
import de.ait.smallBusiness_be.sales.dto.NewShippingDto;
import de.ait.smallBusiness_be.sales.dto.ShippingDto;
import de.ait.smallBusiness_be.sales.services.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShippingController implements ShippingApi {

    private final ShippingService shippingService;

    @Override
    public ShippingDto addShipping(NewShippingDto newShippingDto) {
        return shippingService.createShipping(newShippingDto);
    }

    @Override
    public List<ShippingDto> getAllShippings() {
        return shippingService.findAllShipping();
    }

    @Override
    public ShippingDto getShippingById(Long id) {
        return shippingService.getShippingById(id);
    }

    @Override
    public ShippingDto updateShipping(Long id, NewShippingDto newShippingDto) {
        return shippingService.updateShipping(id, newShippingDto);
    }

    @Override
    public void deleteShipping(Long id) {
        shippingService.deleteShipping(id);
    }
}
