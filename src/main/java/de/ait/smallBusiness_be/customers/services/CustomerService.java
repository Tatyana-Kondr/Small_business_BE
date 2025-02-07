package de.ait.smallBusiness_be.customers.services;

import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Kondratyeva
 * @date 28.11.2024
 */

public interface CustomerService {

    CustomerDto createCustomer(NewCustomerDto newCustomerDto);
    Page<CustomerDto> getAllCustomers(Pageable pageable);
    Page<CustomerDto> getAllCustomersWithCustomerNumber(Pageable pageable);
    CustomerDto getCustomerById(Long id);
    CustomerDto updateCustomer(Long id, NewCustomerDto newCustomerDto);
    void deleteCustomer(Long id);
    Customer getCustomerOrThrow(Long id);
}
