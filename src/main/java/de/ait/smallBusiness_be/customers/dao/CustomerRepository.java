package de.ait.smallBusiness_be.customers.dao;

import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author admin
 * @date 28.11.2024
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNameAndAddress(String name, Address address);

    Optional<Customer> findByCustomerNumber(String customerNumber);

    Page<Customer> findAllByCustomerNumberIsNotNull(Pageable pageable);
}
