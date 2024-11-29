package de.ait.smallBusiness_be.customers.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.dto.AddressDto;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static de.ait.smallBusiness_be.exceptions.ErrorDescription.CUSTOMER_ALREADY_EXISTS;


/**
 * @author Kondratyeva
 * @date 28.11.2024
 */

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements  CustomerService{

    private final CustomerRepository customerRepository;
    private final ModelMapper modelMapper;

    @Override
    public CustomerDto createCustomer(NewCustomerDto newCustomerDto) {

        Customer customer = modelMapper.map(newCustomerDto, Customer.class);
        customer.setAddress(modelMapper.map(newCustomerDto.getAddressDto(), Address.class));

        if (customerRepository.findByNameAndAddress(customer.getName(), customer.getAddress()).isPresent()){
            throw new RestApiException(CUSTOMER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        Customer savedCustomer = customerRepository.save(customer);
        CustomerDto customerDto = modelMapper.map(savedCustomer, CustomerDto.class);
        customerDto.setAddressDto(modelMapper.map(customer.getAddress(), AddressDto.class));
        return customerDto;
    }

}
