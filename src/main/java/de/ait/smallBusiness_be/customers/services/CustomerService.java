package de.ait.smallBusiness_be.customers.services;

import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;

/**
 * @author Kondratyeva
 * @date 28.11.2024
 */

public interface CustomerService {

    public CustomerDto createCustomer(NewCustomerDto newCustomerDto);
}
