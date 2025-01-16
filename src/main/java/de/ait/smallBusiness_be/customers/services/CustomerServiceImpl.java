package de.ait.smallBusiness_be.customers.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.dto.AddressDto;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    @Transactional
    public CustomerDto createCustomer(NewCustomerDto newCustomerDto) {

        checkCustomer(newCustomerDto);

        Customer customer = modelMapper.map(newCustomerDto, Customer.class);        

        Customer savedCustomer = customerRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerDto.class);
    }

    @Override
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAll(pageable);

        if (customers.isEmpty()) {
            throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);
        }
        return customers.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    public Page<CustomerDto> getAllCustomersWithCustomerNumber(Pageable pageable) {
        Page <Customer> customers = customerRepository.findAllByCustomerNumberIsNotNull(pageable);
        return customers.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        Customer customer = getCustomerOrThrow(id);
        return modelMapper.map(customer, CustomerDto.class);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long id, NewCustomerDto newCustomerDto) {

        Customer customer = getCustomerOrThrow(id);

        checkCustomerOnUpdate(id, newCustomerDto);

        if (newCustomerDto.getName()!=null) {
            customer.setName(newCustomerDto.getName());
        }
        if (newCustomerDto.getCustomerNumber()!=null) {
            customer.setCustomerNumber(newCustomerDto.getCustomerNumber());
        }

        if (newCustomerDto.getAddressDto() != null) {
            Address address = customer.getAddress();
            if (address == null) {
                customer.setAddress(modelMapper.map(newCustomerDto.getAddressDto(), Address.class));
            } else {
        // Вместо создания нового объекта Address,
        // существующий объект address обновляется новыми данными из DTO, сохраняя ссылку на оригинальный объект.
                modelMapper.map(newCustomerDto.getAddressDto(), address);
            }
        }

        if (newCustomerDto.getPhone()!=null) {
            customer.setPhone(newCustomerDto.getPhone());
        }
        if (newCustomerDto.getEmail()!=null) {
            customer.setEmail(newCustomerDto.getEmail());
        }
        if (newCustomerDto.getWebsite()!=null) {
            customer.setWebsite(newCustomerDto.getWebsite());
        }

        // Сохранение изменений в базе
        Customer customerSaved = customerRepository.save(customer);

        // Возвращение обновленного клиента как DTO
        return modelMapper.map(customerSaved, CustomerDto.class);
    }

    @Override
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    private void checkCustomer(NewCustomerDto newCustomerDto) {
        if (customerRepository.findByNameAndAddress(
                newCustomerDto.getName(),
                modelMapper.map(newCustomerDto.getAddressDto(), Address.class)).isPresent()
                || customerRepository.findByCustomerNumber(newCustomerDto.getCustomerNumber()).isPresent() ) {
            throw new RestApiException(CUSTOMER_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }
    }

    private Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RestApiException(ErrorDescription.CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND));

    }

    private void checkCustomerOnUpdate(Long id, NewCustomerDto newCustomerDto) {
        // Проверка на совпадение имени и адреса
        customerRepository.findByNameAndAddress(
                        newCustomerDto.getName(),
                        modelMapper.map(newCustomerDto.getAddressDto(), Address.class))
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new RestApiException(CUSTOMER_ALREADY_EXISTS, HttpStatus.CONFLICT);
                    }
                });
        // Проверка на совпадение клиентского номера
        customerRepository.findByCustomerNumber(newCustomerDto.getCustomerNumber())
                .ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(id)) {
                        throw new RestApiException(CUSTOMER_ALREADY_EXISTS, HttpStatus.CONFLICT);
                    }
                });
    }
}
