
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.dto.AddressDto;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.CustomerPickDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.customers.services.CustomerServiceImpl;
import de.ait.smallBusiness_be.exceptions.FieldValidationException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private WarehouseRecordRepository warehouseRecordRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private NewCustomerDto newCustomerDto;
    private Customer customer;
    private CustomerDto customerDto;
    private Address address;

    @BeforeEach
    void setUp() {
        address = new Address();
        address.setCountry("DE");
        address.setPostalCode("32657");
        address.setCity("Lemgo");
        address.setStreet("Musterstraße");
        address.setBuilding("10");

        AddressDto addressDto = new AddressDto();
        addressDto.setCountry("DE");
        addressDto.setPostalCode("32657");
        addressDto.setCity("Lemgo");
        addressDto.setStreet("Musterstraße");
        addressDto.setBuilding("10");

        newCustomerDto = new NewCustomerDto();
        newCustomerDto.setName("Muster GmbH");
        newCustomerDto.setCustomerNumber("1001");
        newCustomerDto.setAddressDto(addressDto);

        customer = new Customer();
        customer.setId(1L);
        customer.setName("Muster GmbH");
        customer.setCustomerNumber("1001");
        customer.setAddress(address);

        customerDto = new CustomerDto();
        customerDto.setId(1L);
        customerDto.setName("Muster GmbH");
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.findByCustomerNumber("1001")).thenReturn(Optional.empty());
        when(modelMapper.map(newCustomerDto.getAddressDto(), Address.class)).thenReturn(address);
        when(customerRepository.findAll()).thenReturn(List.of());
        when(modelMapper.map(newCustomerDto, Customer.class)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(customerDto);

        CustomerDto result = customerService.createCustomer(newCustomerDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(customerRepository).save(customer);
    }

    @Test
    void createCustomer_duplicateCustomerNumber() {
        Customer existing = new Customer();
        existing.setId(2L);
        existing.setCustomerNumber("1001");

        when(customerRepository.findByCustomerNumber("1001")).thenReturn(Optional.of(existing));
        when(modelMapper.map(newCustomerDto.getAddressDto(), Address.class)).thenReturn(address);
        when(customerRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> customerService.createCustomer(newCustomerDto)).isInstanceOf(FieldValidationException.class);

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void createCustomer_withoutCustomerNumber() {
        newCustomerDto.setCustomerNumber("   ");

        when(modelMapper.map(newCustomerDto.getAddressDto(), Address.class)).thenReturn(address);
        when(customerRepository.findAll()).thenReturn(List.of());
        when(modelMapper.map(newCustomerDto, Customer.class)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(customerDto);

        customerService.createCustomer(newCustomerDto);

        assertThat(newCustomerDto.getCustomerNumber()).isNull();

        verify(customerRepository, never()).findByCustomerNumber(anyString());
    }

    @Test
    void getAllCustomers_success() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Customer> page = new PageImpl<>(List.of(customer));

        when(customerRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(customerDto);

        Page<CustomerDto> result = customerService.getAllCustomers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getAllCustomersWithCustomerNumber_success() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(customerRepository
                .findAllByCustomerNumberIsNotNullAndCustomerNumberNot(pageable, ""))
                .thenReturn(new PageImpl<>(List.of(customer)));

        when(modelMapper.map(customer, CustomerDto.class)).thenReturn(customerDto);

        Page<CustomerDto> result = customerService.getAllCustomersWithCustomerNumber(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getCustomers_success() {
        CustomerPickDto pickDto = new CustomerPickDto();

        when(customerRepository.findAll(
                Sort.by(Sort.Direction.ASC, "name")
        )).thenReturn(List.of(customer));

        when(modelMapper.map(customer, CustomerPickDto.class))
                .thenReturn(pickDto);

        List<CustomerPickDto> result =
                customerService.getCustomers();

        assertThat(result).hasSize(1);
    }

    @Test
    void getCustomersWithCustomerNumber_success() {
        CustomerPickDto pickDto = new CustomerPickDto();
        pickDto.setName("Muster GmbH");

        when(customerRepository
                .findAllByCustomerNumberIsNotNullAndCustomerNumberNot("")
        ).thenReturn(List.of(customer));

        when(modelMapper.map(customer, CustomerPickDto.class))
                .thenReturn(pickDto);

        List<CustomerPickDto> result =
                customerService.getCustomersWithCustomerNumber();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName())
                .isEqualTo("Muster GmbH");
    }

    @Test
    void getCustomerById_success() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(modelMapper.map(customer, CustomerDto.class))
                .thenReturn(customerDto);

        CustomerDto result =
                customerService.getCustomerById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCustomerById_notFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customerService.getCustomerById(1L)
        ).isInstanceOf(EntityNotFoundException.class);
    }


    @Test
    void updateCustomer_success() {
        newCustomerDto.setName("Neue Muster GmbH");

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(customerRepository.findByCustomerNumber("1001"))
                .thenReturn(Optional.of(customer));

        when(modelMapper.map(
                newCustomerDto.getAddressDto(),
                Address.class
        )).thenReturn(address);

        when(customerRepository.findAll())
                .thenReturn(List.of(customer));

        // Обновление существующего адреса
        doNothing().when(modelMapper).map(
                any(AddressDto.class),
                any(Address.class)
        );

        when(customerRepository.save(customer))
                .thenReturn(customer);

        // Преобразование Customer -> CustomerDto
        doReturn(customerDto)
                .when(modelMapper)
                .map(
                        any(Customer.class),
                        eq(CustomerDto.class)
                );

        CustomerDto result =
                customerService.updateCustomer(1L, newCustomerDto);

        assertThat(result).isNotNull();

        assertThat(customer.getName())
                .isEqualTo("Neue Muster GmbH");

        assertThat(customer.getCustomerNumber())
                .isEqualTo("1001");

        verify(modelMapper).map(
                newCustomerDto.getAddressDto(),
                address
        );

        verify(customerRepository).save(customer);
    }


    @Test
    void updateCustomer_notFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customerService.updateCustomer(1L, newCustomerDto)
        ).isInstanceOf(EntityNotFoundException.class);

        verify(customerRepository, never())
                .save(any(Customer.class));
    }

    @Test
    void deleteCustomer_success() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(customer);
        verify(customerRepository).flush();
    }

    @Test
    void deleteCustomer_usedInPurchases() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(purchaseRepository.existsByVendor_Id(1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                customerService.deleteCustomer(1L)
        ).isInstanceOf(FieldValidationException.class);

        verify(customerRepository, never())
                .delete(any(Customer.class));
    }

    @Test
    void deleteCustomer_usedInSales() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(saleRepository.existsByCustomer_Id(1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                customerService.deleteCustomer(1L)
        ).isInstanceOf(FieldValidationException.class);

        verify(customerRepository, never())
                .delete(any(Customer.class));
    }

    @Test
    void deleteCustomer_usedInWarehouse() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(warehouseRecordRepository.existsByPartner_Id(1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                customerService.deleteCustomer(1L)
        ).isInstanceOf(FieldValidationException.class);

        verify(customerRepository, never())
                .delete(any(Customer.class));
    }

    @Test
    void deleteCustomer_usedInPayments() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(paymentRepository.existsByCustomer_Id(1L))
                .thenReturn(true);

        assertThatThrownBy(() ->
                customerService.deleteCustomer(1L)
        ).isInstanceOf(FieldValidationException.class);

        verify(customerRepository, never())
                .delete(any(Customer.class));
    }

    @Test
    void deleteCustomer_notFound() {
        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                customerService.deleteCustomer(1L)
        ).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void searchCustomers_success() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(customerRepository.findByNameContainingIgnoreCase(
                "Muster", pageable
        )).thenReturn(new PageImpl<>(List.of(customer)));

        when(modelMapper.map(customer, CustomerDto.class))
                .thenReturn(customerDto);

        Page<CustomerDto> result =
                customerService.searchCustomers("Muster", pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchCustomersWithCustomerNumber_success() {
        PageRequest pageable = PageRequest.of(0, 10);

        when(customerRepository
                .findByCustomerNumberIsNotNullAndCustomerNumberNotAndNameContainingIgnoreCase(
                        "", "Muster", pageable
                ))
                .thenReturn(new PageImpl<>(List.of(customer)));

        when(modelMapper.map(customer, CustomerDto.class))
                .thenReturn(customerDto);

        Page<CustomerDto> result =
                customerService.searchCustomersWithCustomerNumber(
                        "Muster", pageable
                );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findPotentialDuplicates_success() {
        when(customerRepository.findAll())
                .thenReturn(List.of(customer));

        when(modelMapper.map(customer, CustomerDto.class))
                .thenReturn(customerDto);

        List<CustomerDto> result =
                customerService.findPotentialDuplicates(
                        "Muster GmbH", null
                );

        assertThat(result).hasSize(1);
    }

    @Test
    void findPotentialDuplicates_blankName() {
        List<CustomerDto> result =
                customerService.findPotentialDuplicates(" ", null);

        assertThat(result).isEmpty();

        verifyNoInteractions(customerRepository);
    }
}
