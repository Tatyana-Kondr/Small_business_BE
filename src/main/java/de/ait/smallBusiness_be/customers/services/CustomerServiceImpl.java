package de.ait.smallBusiness_be.customers.services;

import de.ait.smallBusiness_be.customers.dao.CustomerRepository;
import de.ait.smallBusiness_be.customers.dto.AddressDto;
import de.ait.smallBusiness_be.customers.dto.CustomerDto;
import de.ait.smallBusiness_be.customers.dto.CustomerPickDto;
import de.ait.smallBusiness_be.customers.dto.NewCustomerDto;
import de.ait.smallBusiness_be.customers.model.Address;
import de.ait.smallBusiness_be.customers.model.Customer;
import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.FieldValidationException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.validation.dto.ValidationErrorDto;
import de.ait.smallBusiness_be.warehouse.dao.WarehouseRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static de.ait.smallBusiness_be.exceptions.ErrorDescription.CUSTOMER_ALREADY_EXISTS;
import static de.ait.smallBusiness_be.exceptions.ErrorDescription.CUSTOMER_NUMBER_ALREADY_EXISTS;


@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final SaleRepository saleRepository;
    private final WarehouseRecordRepository warehouseRecordRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CustomerDto createCustomer(NewCustomerDto newCustomerDto) {

        newCustomerDto.setCustomerNumber(normalizeCustomerNumber(newCustomerDto.getCustomerNumber()));

        validateCustomerUniqueness(null, newCustomerDto);

        Customer customer = modelMapper.map(newCustomerDto, Customer.class);

        Customer savedCustomer = customerRepository.save(customer);
        return modelMapper.map(savedCustomer, CustomerDto.class);
    }

    @Override
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        return customerRepository
                .findAll(pageable)
                .map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    public Page<CustomerDto> getAllCustomersWithCustomerNumber(Pageable pageable) {
        Page<Customer> customers = customerRepository.findAllByCustomerNumberIsNotNullAndCustomerNumberNot(pageable, "");
        return customers.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    public List<CustomerPickDto> getCustomers() {
        return customerRepository
                .findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(customer -> modelMapper.map(customer, CustomerPickDto.class))
                .toList();
    }

    @Override
    public List<CustomerPickDto> getCustomersWithCustomerNumber() {
        return customerRepository
                .findAllByCustomerNumberIsNotNullAndCustomerNumberNot("")
                .stream()
                .map(customer ->
                        modelMapper.map(customer, CustomerPickDto.class))
                .sorted(Comparator.comparing(
                        CustomerPickDto::getName,
                        String.CASE_INSENSITIVE_ORDER
                ))
                .toList();
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
        newCustomerDto.setCustomerNumber(normalizeCustomerNumber(newCustomerDto.getCustomerNumber()));

        validateCustomerUniqueness(id, newCustomerDto);

        customer.setName(newCustomerDto.getName());
        customer.setCustomerNumber(newCustomerDto.getCustomerNumber());

        AddressDto addressDto = newCustomerDto.getAddressDto();

        Address address = customer.getAddress();

        if (address == null) {
            customer.setAddress(modelMapper.map(addressDto, Address.class));
        } else {
            modelMapper.map(addressDto, address);
        }

        customer.setPhone(newCustomerDto.getPhone());
        customer.setEmail(newCustomerDto.getEmail());
        customer.setWebsite(newCustomerDto.getWebsite());

        Customer savedCustomer = customerRepository.save(customer);

        return modelMapper.map(savedCustomer, CustomerDto.class);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {

        Customer customer = getCustomerOrThrow(id);

        List<String> usedIn = new ArrayList<>();

        if (purchaseRepository.existsByVendor_Id(id)) {
            usedIn.add("Einkäufen");
        }

        if (saleRepository.existsByCustomer_Id(id)) {
            usedIn.add("Verkäufen");
        }

        if (warehouseRecordRepository.existsByPartner_Id(id)) {
            usedIn.add("Lagerbewegungen");
        }

        if (paymentRepository.existsByCustomer_Id(id)) {
            usedIn.add("Zahlungen");
        }

        if (!usedIn.isEmpty()) {

            String message =
                    "Der Geschäftspartner kann nicht gelöscht werden, da er bereits in folgenden Vorgängen verwendet wird: " +
                            String.join(", ", usedIn) + ".";

            throw new FieldValidationException(
                    List.of(ValidationErrorDto.builder().field("customer").message(message).build()));
        }

        customerRepository.delete(customer);
        customerRepository.flush();
    }

    private void validateCustomerUniqueness(Long currentCustomerId, NewCustomerDto dto) {
        List<ValidationErrorDto> errors = new ArrayList<>();

        String customerNumber = dto.getCustomerNumber();

        // 1. Уникальность Kundennummer
        if (customerNumber != null && !customerNumber.isBlank()) {
            customerRepository.findByCustomerNumber(customerNumber)
                    .filter(existing -> currentCustomerId == null || !Objects.equals(existing.getId(), currentCustomerId))
                    .ifPresent(existing ->
                            errors.add(ValidationErrorDto.builder()
                                            .field("customerNumber")
                                            .message(CUSTOMER_NUMBER_ALREADY_EXISTS.getDescription())
                                            .build()
                            )
                    );
        }

        // 2. Поиск точных дубликатов фирмы
        Address newAddress = modelMapper.map(
                dto.getAddressDto(),
                Address.class
        );

        if (hasCompleteAddress(newAddress)) {
            String normalizedName =
                    CustomerDuplicateNormalizer.normalizeName(dto.getName());

            boolean duplicateExists = customerRepository.findAll()
                    .stream()
                    .filter(existing -> currentCustomerId == null || !Objects.equals(existing.getId(), currentCustomerId))
                    .anyMatch(existing ->
                            CustomerDuplicateNormalizer.normalizeName(existing.getName())
                                    .equals(normalizedName) && isSameFullAddress(existing.getAddress(), newAddress));

            if (duplicateExists) {
                errors.add(
                        ValidationErrorDto.builder()
                                .field("name")
                                .message(CUSTOMER_ALREADY_EXISTS.getDescription())
                                .build()
                );
            }
        }

        if (!errors.isEmpty()) {
            throw new FieldValidationException(errors);
        }
    }

    public Customer getCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->  new EntityNotFoundException(ErrorDescription.CUSTOMER_NOT_FOUND.getDescription()));
    }

    @Override
    public Page<CustomerDto> searchCustomers(String query, Pageable pageable) {
        Page<Customer> customers = customerRepository.findByNameContainingIgnoreCase(query, pageable);

        return customers.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    public Page<CustomerDto> searchCustomersWithCustomerNumber(String query, Pageable pageable) {
        Page<Customer> customers = customerRepository.findByCustomerNumberIsNotNullAndCustomerNumberNotAndNameContainingIgnoreCase("", query, pageable);

        return customers.map(customer -> modelMapper.map(customer, CustomerDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerDto> findPotentialDuplicates(
            String name,
            Long excludeId
    ) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        String normalizedName =
                CustomerDuplicateNormalizer.normalizeName(name);

        return customerRepository.findAll()
                .stream()
                .filter(customer -> excludeId == null || !Objects.equals(customer.getId(), excludeId))
                .filter(customer -> CustomerDuplicateNormalizer.normalizeName(customer.getName()).equals(normalizedName))
                .map(customer -> modelMapper.map(customer, CustomerDto.class))
                .toList();
    }

    private String normalizeCustomerNumber(String customerNumber) {
        if (customerNumber == null || customerNumber.isBlank()) {
            return null;
        }
        return customerNumber.trim();
    }

    private boolean hasCompleteAddress(Address address) {
        return address != null
                && hasText(address.getCountry())
                && hasText(address.getPostalCode())
                && hasText(address.getCity())
                && hasText(address.getStreet())
                && hasText(address.getBuilding());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean isSameFullAddress(
            Address first,
            Address second
    ) {
        if (!hasCompleteAddress(first) || !hasCompleteAddress(second)) {
            return false;
        }

        return CustomerDuplicateNormalizer.normalizeCountry(first.getCountry())
                .equals(CustomerDuplicateNormalizer.normalizeCountry(second.getCountry()))

                && CustomerDuplicateNormalizer.normalizePostalCode(first.getPostalCode())
                .equals(CustomerDuplicateNormalizer.normalizePostalCode(second.getPostalCode()))

                && CustomerDuplicateNormalizer.normalizeCity(first.getCity())
                .equals(CustomerDuplicateNormalizer.normalizeCity(second.getCity()))

                && CustomerDuplicateNormalizer.normalizeStreet(first.getStreet())
                .equals(CustomerDuplicateNormalizer.normalizeStreet(second.getStreet()))

                && CustomerDuplicateNormalizer.normalizeBuilding(first.getBuilding())
                .equals(CustomerDuplicateNormalizer.normalizeBuilding(second.getBuilding()));
    }
}
