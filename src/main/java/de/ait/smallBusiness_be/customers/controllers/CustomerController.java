package de.ait.smallBusiness_be.customers.controllers;

import de.ait.smallBusiness_be.customers.controllers.api.CustomersApi;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.services.CustomerService;
import lombok.RequiredArgsConstructor;
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
// final AuthService authService;

    @Override
    public CustomerDto createCustomer(NewCustomerDto newCustomerDto
    //, AuthenticatedUser currentUser
    ) {
        // User authenticatedUser = currentUser.getUser();
        return customerService.createCustomer(newCustomerDto
        //, authenticatedUser
        );
    }
}
