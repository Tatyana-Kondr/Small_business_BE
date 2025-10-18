package de.ait.smallBusiness_be.company.dao;

import de.ait.smallBusiness_be.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
