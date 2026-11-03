package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.TermOfPaymentRepository;
import de.ait.smallBusiness_be.sales.dto.NewTermOfPaymentDto;
import de.ait.smallBusiness_be.sales.dto.TermOfPaymentDto;
import de.ait.smallBusiness_be.sales.models.TermOfPayment;
import de.ait.smallBusiness_be.sales.services.TermOfPaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermOfPaymentServiceImpl implements TermOfPaymentService {

    private final TermOfPaymentRepository repository;
    private final SaleRepository saleRepository;
    private final ModelMapper modelMapper;

    @Override
    public TermOfPaymentDto createTermOfPayment(NewTermOfPaymentDto newTermOfPaymentDto) {

        String name = newTermOfPaymentDto.getName().trim();

        if (repository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.TERM_OF_PAYMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        TermOfPayment term = TermOfPayment.builder().name(name).build();
        TermOfPayment savedTerm = repository.save(term);

        return modelMapper.map(savedTerm, TermOfPaymentDto.class);
    }

    @Override
    public List<TermOfPaymentDto> findAllTermsOfPayment() {
        return repository.findAll()
                .stream()
                .map(term -> modelMapper.map(term, TermOfPaymentDto.class))
                .toList();
    }

    @Override
    public TermOfPaymentDto findTermOfPaymentById(long id) {
        TermOfPayment term = getTermOfPaymentOrThrow(id);
        return modelMapper.map(term, TermOfPaymentDto.class);
    }

    @Override
    public TermOfPaymentDto updateTermOfPayment(Long id, NewTermOfPaymentDto newTermOfPaymentDto) {

        TermOfPayment term = getTermOfPaymentOrThrow(id);
        String name = newTermOfPaymentDto.getName().trim();

        if (!term.getName().equals(name) && repository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.TERM_OF_PAYMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        term.setName(name);
        TermOfPayment savedTerm = repository.save(term);

        return modelMapper.map(savedTerm, TermOfPaymentDto.class);
    }

    @Override
    @Transactional
    public void deleteTermOfPayment(long id) {

        TermOfPayment term = getTermOfPaymentOrThrow(id);

        if (saleRepository.existsByTermsOfPayment_Id(id)) {
            throw new RestApiException(ErrorDescription.TERM_OF_PAYMENT_IN_USE, HttpStatus.CONFLICT);
        }

        repository.delete(term);
        repository.flush();
    }

    @Override
    public TermOfPayment getTermOfPaymentOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TERM_OF_PAYMENT_NOT_FOUND.getDescription()));
    }
}
