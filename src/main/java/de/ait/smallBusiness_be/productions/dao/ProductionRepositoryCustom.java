package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.Production;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;


public interface ProductionRepositoryCustom {
    Page<Production> searchProduction(Pageable pageable, String query);
    Page<Production> getAllProductionsByFilter(Pageable pageable, LocalDate startDate, LocalDate endDate, String searchQuery);
}
