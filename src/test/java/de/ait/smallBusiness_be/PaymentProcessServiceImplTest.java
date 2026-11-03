
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentProcessRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import de.ait.smallBusiness_be.payments.services.impl.PaymentProcessServiceImpl;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentProcessServiceImplTest {

    @Mock
    private PaymentProcessRepository repository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentProcessServiceImpl service;

    private PaymentProcess process;
    private PaymentProcessDto processDto;

    @BeforeEach
    void setUp() {
        process = PaymentProcess.builder()
                .processName("Überweisung")
                .build();

        processDto = new PaymentProcessDto();
    }


    @Test
    void createPaymentProcess_success() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Überweisung");

        when(repository.save(any(PaymentProcess.class))).thenReturn(process);
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(processDto);

        PaymentProcessDto result = service.createPaymentProcess(newDto);

        assertThat(result).isSameAs(processDto);

        verify(repository).existsByProcessName("Überweisung");
        verify(repository).save(any(PaymentProcess.class));
    }

    @Test
    void createPaymentProcess_duplicate_throws() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Überweisung");

        when(repository.existsByProcessName("Überweisung")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> service.createPaymentProcess(newDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.PAYMENT_PROCESS_ALREADY_EXISTS.getDescription());

        verify(repository, never()).save(any(PaymentProcess.class));
    }

    @Test
    void createPaymentProcess_trimsName() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("  Überweisung  ");

        when(repository.save(any(PaymentProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(modelMapper.map(any(PaymentProcess.class), eq(PaymentProcessDto.class))).thenReturn(processDto);

        service.createPaymentProcess(newDto);

        ArgumentCaptor<PaymentProcess> captor = ArgumentCaptor.forClass(PaymentProcess.class);

        verify(repository).save(captor.capture());

        assertThat(captor.getValue().getProcessName()).isEqualTo("Überweisung");

        verify(repository).existsByProcessName("Überweisung");
    }

    @Test
    void getAllPaymentProcesses_success() {
        when(repository.findAll()).thenReturn(List.of(process));

        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(processDto);

        List<PaymentProcessDto> result = service.getAllPaymentProcesses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(processDto);

        verify(repository).findAll();
    }

    @Test
    void getAllPaymentProcesses_empty_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<PaymentProcessDto> result = service.getAllPaymentProcesses();

        assertThat(result).isEmpty();
    }

    @Test
    void getPaymentProcessById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(process));
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(processDto);

        PaymentProcessDto result = service.getPaymentProcessById(1L);

        assertThat(result).isSameAs(processDto);
    }

    @Test
    void getPaymentProcessById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getPaymentProcessById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updatePaymentProcess_success() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Barzahlung");

        when(repository.findById(1L)).thenReturn(Optional.of(process));
        when(repository.save(process)).thenReturn(process);
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(processDto);

        PaymentProcessDto result = service.updatePaymentProcess(1L, newDto);

        assertThat(result).isSameAs(processDto);
        assertThat(process.getProcessName()).isEqualTo("Barzahlung");

        verify(repository).existsByProcessName("Barzahlung");
        verify(repository).save(process);
    }

    @Test
    void updatePaymentProcess_sameName_skipsDuplicateCheck() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto(" Überweisung ");

        when(repository.findById(1L)).thenReturn(Optional.of(process));
        when(repository.save(process)).thenReturn(process);
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(processDto);

        service.updatePaymentProcess(1L, newDto);

        verify(repository, never()).existsByProcessName(anyString());
        assertThat(process.getProcessName()).isEqualTo("Überweisung");
    }

    @Test
    void updatePaymentProcess_duplicate_throws() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Barzahlung");

        when(repository.findById(1L)).thenReturn(Optional.of(process));
        when(repository.existsByProcessName("Barzahlung")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> service.updatePaymentProcess(1L, newDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.PAYMENT_PROCESS_ALREADY_EXISTS.getDescription());

        verify(repository, never()).save(any(PaymentProcess.class));
    }

    @Test
    void updatePaymentProcess_notFound_throws() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Barzahlung");
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.updatePaymentProcess(1L, newDto)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deletePaymentProcess_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(process));

        service.deletePaymentProcess(1L);

        verify(paymentRepository).existsByPaymentProcess_Id(1L);
        verify(repository).delete(process);
        verify(repository).flush();
    }

    @Test
    void deletePaymentProcess_inUse_throws() {
        when(repository.findById(1L)).thenReturn(Optional.of(process));

        when(paymentRepository.existsByPaymentProcess_Id(1L)).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> service.deletePaymentProcess(1L), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.PAYMENT_PROCESS_IN_USE.getDescription());

        verify(repository, never()).delete(any(PaymentProcess.class));
        verify(repository, never()).flush();
    }

    @Test
    void deletePaymentProcess_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePaymentProcess(1L)).isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(paymentRepository);
        verify(repository, never()).delete(any(PaymentProcess.class));
    }

    @Test
    void getPaymentProcessOrThrow_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(process));
        PaymentProcess result = service.getPaymentProcessOrThrow(1L);
        assertThat(result).isSameAs(process);
    }

    @Test
    void getPaymentProcessOrThrow_notFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentProcessOrThrow(1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ErrorDescription.PAYMENT_PROCESS_NOT_FOUND.getDescription());
    }
}
