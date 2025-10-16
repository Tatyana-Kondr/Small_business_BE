package de.ait.smallBusiness_be.productions.dao;

import de.ait.smallBusiness_be.productions.model.Production;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * 13.02.2025
 * SmB_be
 *
 * @author Kondratyeva (AIT TR)
 */


public interface ProductionRepository extends JpaRepository<Production, Long>, ProductionRepositoryCustom {

}
