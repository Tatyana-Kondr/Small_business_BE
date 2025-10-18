package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.payments.dao.PaymentProcessRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentProcessDto;
import de.ait.smallBusiness_be.payments.dto.PaymentProcessDto;
import de.ait.smallBusiness_be.payments.model.PaymentProcess;
import de.ait.smallBusiness_be.payments.services.impl.PaymentProcessServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentProcessServiceImplTest {

    @InjectMocks
    private PaymentProcessServiceImpl service;

    @Mock
    private PaymentProcessRepository repository;

    @Mock
    private ModelMapper modelMapper;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // ===== CREATE =====
    @Test
    void createPaymentProcess_success() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Process1");
        PaymentProcess process = PaymentProcess.builder().processName("Process1").build();
        PaymentProcessDto dto = new PaymentProcessDto();

        when(repository.existsByProcessName("Process1")).thenReturn(false);
        when(repository.save(any(PaymentProcess.class))).thenReturn(process);
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(dto);

        PaymentProcessDto result = service.createPaymentProcess(newDto);

        assertThat(result).isNotNull();
        verify(repository).save(any(PaymentProcess.class));
    }

    @Test
    void createPaymentProcess_duplicate_throws() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("Process1");

        when(repository.existsByProcessName("Process1")).thenReturn(true);

        assertThatThrownBy(() -> service.createPaymentProcess(newDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // ===== GET ALL =====
    @Test
    void getAllPaymentProcesses_success() {
        PaymentProcess process = PaymentProcess.builder().processName("Process1").build();
        PaymentProcessDto dto = new PaymentProcessDto();

        when(repository.findAll()).thenReturn(List.of(process));
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(dto);

        List<PaymentProcessDto> result = service.getAllPaymentProcesses();

        assertThat(result).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    void getAllPaymentProcesses_empty_throws() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.getAllPaymentProcesses())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("List is empty");
    }

    // ===== GET BY ID =====
    @Test
    void getPaymentProcessById_success() {
        PaymentProcess process = PaymentProcess.builder().processName("Process1").build();
        PaymentProcessDto dto = new PaymentProcessDto();

        when(repository.findById(1L)).thenReturn(Optional.of(process));
        when(modelMapper.map(process, PaymentProcessDto.class)).thenReturn(dto);

        PaymentProcessDto result = service.getPaymentProcessById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getPaymentProcessById_notFound_throws() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPaymentProcessById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ===== UPDATE =====
    @Test
    void updatePaymentProcess_success() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("UpdatedProcess");
        PaymentProcess existing = PaymentProcess.builder().processName("Process1").build();
        PaymentProcessDto dto = new PaymentProcessDto();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, PaymentProcessDto.class)).thenReturn(dto);

        PaymentProcessDto result = service.updatePaymentProcess(1L, newDto);

        assertThat(result).isNotNull();
        verify(repository).save(existing);
    }

    @Test
    void updatePaymentProcess_notFound_throws() {
        NewPaymentProcessDto newDto = new NewPaymentProcessDto("UpdatedProcess");

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updatePaymentProcess(1L, newDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ===== DELETE =====
    @Test
    void deletePaymentProcess_success() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deletePaymentProcess(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deletePaymentProcess_notFound_throws() {
        when(repository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.deletePaymentProcess(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
