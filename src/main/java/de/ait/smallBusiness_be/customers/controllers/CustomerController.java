package de.ait.smallBusiness_be.customers.controllers;

import de.ait.smallBusiness_be.customers.controllers.api.CustomersApi;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Kondratyeva
 * @date 28.11.2024
 */

@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {
 private final CustomerService customerService;

    @Override
    public CustomerDto createCustomer(NewCustomerDto newCustomerDto) {

        return customerService.createCustomer(newCustomerDto);
    }

    @Override
    public Page<CustomerDto> getAllCustomers(Pageable pageable, String sort) {
        return customerService.getAllCustomers(pageable);
    }

    @Override
    public Page<CustomerDto> getAllCustomersWithCustomerNumber(Pageable pageable, String sort) {
        return customerService.getAllCustomersWithCustomerNumber(pageable);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        return customerService.getCustomerById(id);
    }

    @Override
    public CustomerDto updateCustomer(Long id, NewCustomerDto newCustomerDto) {
        return customerService.updateCustomer(id, newCustomerDto);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerService.deleteCustomer(id);
    }
}
