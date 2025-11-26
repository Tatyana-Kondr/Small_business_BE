package de.ait.smallBusiness_be.sales.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermOfPaymentServiceImpl implements TermOfPaymentService {

    private final TermOfPaymentRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public TermOfPaymentDto createTermOfPayment(NewTermOfPaymentDto newTermOfPaymentDto) {
        if (repository.existsByName(newTermOfPaymentDto.getName())) {
            throw new IllegalArgumentException("Term Of Payment with name '" + newTermOfPaymentDto.getName() + "' already exists");
        }
        TermOfPayment term = TermOfPayment.builder().name(newTermOfPaymentDto.getName()).build();
        TermOfPayment savedTerm = repository.save(term);
        return modelMapper.map(savedTerm, TermOfPaymentDto.class);
    }

    @Override
    public List<TermOfPaymentDto> findAllTermsOfPayment() {
        List<TermOfPayment> terms = repository.findAll();
        if (terms.isEmpty()) {throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return terms.stream().map(term -> modelMapper.map(term, TermOfPaymentDto.class)).collect(Collectors.toList());
    }

    @Override
    public TermOfPaymentDto findTermOfPaymentById(long id) {
        TermOfPayment term = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Term Of Payment not found"));
        return modelMapper.map(term, TermOfPaymentDto.class);
    }

    @Override
    public TermOfPaymentDto updateTermOfPayment(Long id, NewTermOfPaymentDto newTermOfPaymentDto) {
        TermOfPayment term = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Term Of Payment not found"));
        term.setName(newTermOfPaymentDto.getName());
        TermOfPayment savedTerm = repository.save(term);
        return modelMapper.map(savedTerm, TermOfPaymentDto.class);
    }

    @Override
    public void deleteTermOfPayment(long id) {
        if (repository.existsById(id)) { throw new EntityNotFoundException("Term Of Payment with id '" + id + "' not found"); }
        repository.deleteById(id);
    }
}
