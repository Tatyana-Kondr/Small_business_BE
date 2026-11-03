package de.ait.smallBusiness_be.warehouse.dao;

import de.ait.smallBusiness_be.purchases.model.TypeOfOperation;
import de.ait.smallBusiness_be.warehouse.models.WarehouseRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;


@Repository
public interface WarehouseRecordRepository extends JpaRepository<WarehouseRecord, Long> {
    Page<WarehouseRecord> findAllByProductIdOrderByDateDesc(Long productId, Pageable pageable);
    List<WarehouseRecord> findAllByDocumentId(Long documentId);
    List<WarehouseRecord> findAllByDocumentIdAndTypeOfOperationIn(Long documentId, Collection<TypeOfOperation> types);
    boolean existsByPartner_Id(Long partnerId);
    boolean existsByProduct_Id(Long productId);
}
