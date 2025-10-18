package de.ait.smallBusiness_be;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.payments.dao.PaymentMethodRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import de.ait.smallBusiness_be.payments.services.impl.PaymentMethodServiceImpl;
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

class PaymentMethodServiceImplTest {

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

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
    void createPaymentMethod_success() {
        NewPaymentMethodDto newDto = new NewPaymentMethodDto("VISA", "****1234", "details", true);
        PaymentMethod method = PaymentMethod.builder()
                .provider("VISA")
                .maskedNumber("****1234")
                .details("details")
                .active(true)
                .build();
        PaymentMethodDto dto = new PaymentMethodDto();

        when(paymentMethodRepository.existsPaymentMethodByProviderAndMaskedNumber("VISA", "****1234"))
                .thenReturn(false);
        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(method);
        when(modelMapper.map(method, PaymentMethodDto.class)).thenReturn(dto);

        PaymentMethodDto result = paymentMethodService.createPaymentMethod(newDto);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository).save(any(PaymentMethod.class));
    }


    @Test
    void createPaymentMethod_duplicate_throws() {
        NewPaymentMethodDto newDto = new NewPaymentMethodDto("VISA", "****1234", "details", true);

        when(paymentMethodRepository.existsPaymentMethodByProviderAndMaskedNumber("VISA", "****1234"))
                .thenReturn(true);

        assertThatThrownBy(() -> paymentMethodService.createPaymentMethod(newDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // ===== GET ALL =====
    @Test
    void getAllPaymentMethods_success() {
        PaymentMethod entity = PaymentMethod.builder().provider("VISA").maskedNumber("****1234").build();
        PaymentMethodDto dto = new PaymentMethodDto();

        when(paymentMethodRepository.findAll()).thenReturn(List.of(entity));
        when(modelMapper.map(entity, PaymentMethodDto.class)).thenReturn(dto);

        List<PaymentMethodDto> result = paymentMethodService.getAllPaymentMethods();

        assertThat(result).hasSize(1);
        verify(paymentMethodRepository).findAll();
    }

    @Test
    void getAllPaymentMethods_empty_throws() {
        when(paymentMethodRepository.findAll()).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> paymentMethodService.getAllPaymentMethods())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("List is empty");
    }

    // ===== GET BY ID =====
    @Test
    void getPaymentMethodById_success() {
        PaymentMethod entity = PaymentMethod.builder().id(1L).provider("VISA").build();
        PaymentMethodDto dto = new PaymentMethodDto();

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(modelMapper.map(entity, PaymentMethodDto.class)).thenReturn(dto);

        PaymentMethodDto result = paymentMethodService.getPaymentMethodById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getPaymentMethodById_notFound_throws() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.getPaymentMethodById(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ===== UPDATE =====
    @Test
    void updatePaymentMethod_success() {
        NewPaymentMethodDto newDto = new NewPaymentMethodDto("VISA", "****1234", "updated", true);
        PaymentMethod existing = PaymentMethod.builder().id(1L).provider("VISA").maskedNumber("****1234").build();
        PaymentMethodDto dto = new PaymentMethodDto();

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentMethodRepository.existsPaymentMethodByProviderAndMaskedNumber("VISA", "****1234"))
                .thenReturn(false);
        when(paymentMethodRepository.save(existing)).thenReturn(existing);
        when(modelMapper.map(existing, PaymentMethodDto.class)).thenReturn(dto);

        PaymentMethodDto result = paymentMethodService.updatePaymentMethod(1L, newDto);

        assertThat(result).isNotNull();
        verify(paymentMethodRepository).save(existing);
    }

    @Test
    void updatePaymentMethod_notFound_throws() {
        NewPaymentMethodDto newDto = new NewPaymentMethodDto("VISA", "****1234", "updated", true);

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.updatePaymentMethod(1L, newDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ===== DELETE =====
    @Test
    void deletePaymentMethod_success() {
        when(paymentMethodRepository.existsById(1L)).thenReturn(true);

        paymentMethodService.deletePaymentMethod(1L);

        verify(paymentMethodRepository).deleteById(1L);
    }

    @Test
    void deletePaymentMethod_notFound_throws() {
        when(paymentMethodRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> paymentMethodService.deletePaymentMethod(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}