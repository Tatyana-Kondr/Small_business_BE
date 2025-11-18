package de.ait.smallBusiness_be.warehouse.services;

import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.products.model.Product;
import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseRecordDto;
import de.ait.smallBusiness_be.warehouse.dto.WarehouseStockDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface WarehouseService {
    void recordOperation(Product product, TypeOfOperation type, BigDecimal qty, Long documentId, Customer partner, LocalDate date);
    WarehouseStockDto getStock(Long productId);
    Page<WarehouseRecordDto> getProductHistory(Long productId, Pageable pageable);
    Page<WarehouseStockDto> getAllStocks(Pageable pageable);
    <T> void syncDocument(TypeOfOperation type, Long documentId, Customer partner, LocalDate date, List<T> items);
    void rollbackDocument(Long documentId);
}
