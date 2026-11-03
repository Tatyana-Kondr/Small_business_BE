
package de.ait.smallBusiness_be.payments.services.impl;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentProcessRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import de.ait.smallBusiness_be.payments.services.PaymentProcessService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentProcessServiceImpl implements PaymentProcessService {

    private final PaymentProcessRepository repository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public PaymentProcessDto createPaymentProcess(NewPaymentProcessDto newPaymentProcessDto) {

        String processName = newPaymentProcessDto.getProcessName().trim();

        if (repository.existsByProcessName(processName)) {
            throw new RestApiException(ErrorDescription.PAYMENT_PROCESS_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        PaymentProcess process = PaymentProcess.builder()
                .processName(processName)
                .build();

        PaymentProcess savedProcess = repository.save(process);

        return modelMapper.map(savedProcess, PaymentProcessDto.class);
    }

    @Override
    public List<PaymentProcessDto> getAllPaymentProcesses() {
        return repository.findAll()
                .stream()
                .map(process -> modelMapper.map(process, PaymentProcessDto.class))
                .toList();
    }

    @Override
    public PaymentProcessDto getPaymentProcessById(Long id) {

        PaymentProcess process = getPaymentProcessOrThrow(id);

        return modelMapper.map(process, PaymentProcessDto.class);
    }

    @Override
    public PaymentProcessDto updatePaymentProcess(Long id, NewPaymentProcessDto newPaymentProcessDto) {

        PaymentProcess process = getPaymentProcessOrThrow(id);

        String processName = newPaymentProcessDto.getProcessName().trim();

        if (!process.getProcessName().equals(processName) && repository.existsByProcessName(processName)) {
            throw new RestApiException(ErrorDescription.PAYMENT_PROCESS_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        process.setProcessName(processName);

        PaymentProcess savedProcess = repository.save(process);

        return modelMapper.map(savedProcess, PaymentProcessDto.class);
    }

    @Override
    @Transactional
    public void deletePaymentProcess(Long id) {

        PaymentProcess process = getPaymentProcessOrThrow(id);

        if (paymentRepository.existsByPaymentProcess_Id(id)) {
            throw new RestApiException(ErrorDescription.PAYMENT_PROCESS_IN_USE, HttpStatus.CONFLICT);
        }

        repository.delete(process);
        repository.flush();
    }

    @Override
    public PaymentProcess getPaymentProcessOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(ErrorDescription.PAYMENT_PROCESS_NOT_FOUND.getDescription()));
    }
}
