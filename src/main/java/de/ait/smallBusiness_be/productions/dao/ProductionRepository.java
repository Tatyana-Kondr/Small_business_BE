package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.Production;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */

@Repository
public interface ProductionRepository extends JpaRepository<Production, Long> {
}
