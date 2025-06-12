package de.ait.smallBusiness_be.sales.controllers;

import de.ait.smallBusiness_be.sales.controllers.api.SalesApi;
import de.ait.smallBusiness_be.sales.dto.NewSaleDto;
import de.ait.smallBusiness_be.sales.dto.SaleDto;
import de.ait.smallBusiness_be.sales.services.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * 18.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@RestController
@RequiredArgsConstructor
public class SaleController implements SalesApi {

    private  final SaleService saleService;

    @Override
    public SaleDto addSale(NewSaleDto newSaleDto) {
        return saleService.createSale(newSaleDto);
    }
}
