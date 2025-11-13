package de.ait.smallBusiness_be.warehouse.controllers;

import de.ait.smallBusiness_be.warehouse.dto.WarehouseRecordDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseStockDto;
import de.ait.smallBusiness_be.warehouse.services.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi{

    private final WarehouseService warehouseService;

    @Override
    public Page<WarehouseStockDto> getAllStocks(Pageable pageable) {
        return warehouseService.getAllStocks(pageable);
    }

    @Override
    public WarehouseStockDto getProductStock(Long productId) {
        return warehouseService.getStock(productId);
    }

    @Override
    public Page<WarehouseRecordDto> getProductHistory(Long productId, Pageable pageable) {
        return warehouseService.getProductHistory(productId, pageable);
    }
}
