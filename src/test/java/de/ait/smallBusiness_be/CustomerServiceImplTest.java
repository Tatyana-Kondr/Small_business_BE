package de.ait.smallBusiness_be;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.dto.AddressDto;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private org.modelmapper.ModelMapper modelMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createCustomer_success() {
        NewCustomerDto newCustomerDto = new NewCustomerDto();
        newCustomerDto.setName("John Doe");
        AddressDto addressDto = new AddressDto();
        newCustomerDto.setAddressDto(addressDto);

        Customer customer = new Customer();
        Customer savedCustomer = new Customer();
        CustomerDto customerDto = new CustomerDto();

        when(modelMapper.map(newCustomerDto, Customer.class)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(savedCustomer);
        when(modelMapper.map(savedCustomer, CustomerDto.class)).thenReturn(customerDto);
        when(customerRepository.existsByNameAndAddress(any(), any())).thenReturn(false);
        when(customerRepository.existsByCustomerNumber(any())).thenReturn(false);

        CustomerDto result = customerService.createCustomer(newCustomerDto);

        assertThat(result).isNotNull();
        verify(customerRepository).save(customer);
    }

    @Test
    void getAllCustomers_success() {
        Customer customer = new Customer();
        Page<Customer> page = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);
        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(new CustomerDto());

        Page<CustomerDto> result = customerService.getAllCustomers(PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(customerRepository).findAll(PageRequest.of(0, 10));
    }

    @Test
    void getCustomerById_success() {
        Customer customer = new Customer();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(new CustomerDto());

        CustomerDto result = customerService.getCustomerById(1L);

        assertThat(result).isNotNull();
        verify(customerRepository).findById(1L);
    }

    @Test
    void updateCustomer_success() {
        NewCustomerDto newCustomerDto = new NewCustomerDto();
        newCustomerDto.setName("New Name");
        Customer customer = new Customer();
        Customer updatedCustomer = new Customer();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(modelMapper.map(newCustomerDto.getAddressDto(), Address.class)).thenReturn(new Address());
        when(customerRepository.save(customer)).thenReturn(updatedCustomer);
        when(modelMapper.map(updatedCustomer, CustomerDto.class)).thenReturn(new CustomerDto());

        CustomerDto result = customerService.updateCustomer(1L, newCustomerDto);

        assertThat(result).isNotNull();
        verify(customerRepository).save(customer);
    }

    @Test
    void deleteCustomer_success() {
        Customer customer = new Customer();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
    }
}

