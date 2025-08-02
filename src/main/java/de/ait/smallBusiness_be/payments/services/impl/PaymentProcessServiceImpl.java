package de.ait.smallBusiness_be.payments.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentProcessRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import de.ait.smallBusiness_be.payments.services.PaymentProcessService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentProcessServiceImpl implements PaymentProcessService {

    private final PaymentProcessRepository repository;
    private final ModelMapper modelMapper;
    private final PaymentProcessRepository paymentProcessRepository;

    @Override
    public PaymentProcessDto createPaymentProcess(NewPaymentProcessDto newPaymentProcessDto) {
        if (repository.existsByProcessName(newPaymentProcessDto.getProcessName())) {
            throw new IllegalArgumentException("Payment process with name '" + newPaymentProcessDto.getProcessName() + "' already exists");
        }

        PaymentProcess process = PaymentProcess.builder()
                .processName(newPaymentProcessDto.getProcessName())
                .build();

        PaymentProcess savedProcess = repository.save(process);
        return modelMapper.map(savedProcess, PaymentProcessDto.class);
    }

    @Override
    public List<PaymentProcessDto> getAllPaymentProcesses() {
        List<PaymentProcess> processes = repository.findAll();
        if (processes.isEmpty()) { throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return processes.stream().map(process -> modelMapper.map(process, PaymentProcessDto.class)).collect(Collectors.toList());
    }

    @Override
    public PaymentProcessDto getPaymentProcessById(Long id) {
        PaymentProcess process = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment process not found"));
        return modelMapper.map(process, PaymentProcessDto.class);
    }

    @Override
    public PaymentProcessDto updatePaymentProcess(Long id, NewPaymentProcessDto newPaymentProcessDto) {
        PaymentProcess process = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Payment process not found"));
        process.setProcessName(newPaymentProcessDto.getProcessName());
        PaymentProcess savedProcess = repository.save(process);
        return modelMapper.map(savedProcess, PaymentProcessDto.class);
    }

    @Override
    public void deletePaymentProcess(Long id) {
        if (!paymentProcessRepository.existsById(id)) {
          throw new EntityNotFoundException("Payment process not found");
        }
        repository.deleteById(id);
    }
}
