package de.ait.smallBusiness_be.customers.dao;

import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * @author admin
 * @date 28.11.2024
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNameAndAddress(String name, Address address);
}
