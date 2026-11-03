
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentMethodRepository;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.payments.dto.NewPaymentMethodDto;
import de.ait.smallBusiness_be.payments.dto.PaymentMethodDto;
import de.ait.smallBusiness_be.payments.model.PaymentMethod;
import de.ait.smallBusiness_be.payments.services.impl.PaymentMethodServiceImpl;
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
class PaymentMethodServiceImplTest {

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PaymentMethodServiceImpl paymentMethodService;

    private PaymentMethod paymentMethod;
    private PaymentMethodDto paymentMethodDto;

    @BeforeEach
    void setUp() {
        paymentMethod = PaymentMethod.builder()
                .id(1L)
                .provider("VISA")
                .maskedNumber("****1234")
                .details("Test details")
                .active(true)
                .build();

        paymentMethodDto = new PaymentMethodDto();
    }

    @Test
    void createPaymentMethod_success() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "VISA",
                        "****1234",
                        "details",
                        true
                );

        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenReturn(paymentMethod);
        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        PaymentMethodDto result = paymentMethodService.createPaymentMethod(newDto);

        assertThat(result).isNotNull();

        verify(paymentMethodRepository).existsPaymentMethodByProviderAndMaskedNumber("VISA", "****1234");
        verify(paymentMethodRepository).save(any(PaymentMethod.class));
    }

    @Test
    void createPaymentMethod_duplicate_throws() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "VISA",
                        "****1234",
                        "details",
                        true
                );

        when(paymentMethodRepository.existsPaymentMethodByProviderAndMaskedNumber("VISA", "****1234")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(
                () -> paymentMethodService.createPaymentMethod(newDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.PAYMENT_METHOD_ALREADY_EXISTS.getDescription());

        verify(paymentMethodRepository, never()).save(any(PaymentMethod.class));
    }

    @Test
    void createPaymentMethod_normalizesFields() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "  VISA  ",
                        "  ****1234  ",
                        "   ",
                        true
                );

        when(paymentMethodRepository.save(any(PaymentMethod.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(modelMapper.map(any(PaymentMethod.class), eq(PaymentMethodDto.class))).thenReturn(paymentMethodDto);

        paymentMethodService.createPaymentMethod(newDto);

        ArgumentCaptor<PaymentMethod> captor = ArgumentCaptor.forClass(PaymentMethod.class);

        verify(paymentMethodRepository).save(captor.capture());

        PaymentMethod saved = captor.getValue();

        assertThat(saved.getProvider()).isEqualTo("VISA");
        assertThat(saved.getMaskedNumber()).isEqualTo("****1234");
        assertThat(saved.getDetails()).isNull();
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void getAllPaymentMethods_success() {
        when(paymentMethodRepository.findAll()).thenReturn(List.of(paymentMethod));
        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        List<PaymentMethodDto> result = paymentMethodService.getAllPaymentMethods();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(paymentMethodDto);
    }

    @Test
    void getAllPaymentMethods_empty_returnsEmptyList() {
        when(paymentMethodRepository.findAll()).thenReturn(List.of());

        List<PaymentMethodDto> result = paymentMethodService.getAllPaymentMethods();

        assertThat(result).isEmpty();
    }

    @Test
    void getActivePaymentMethods_success() {
        when(paymentMethodRepository
                .findAllByActiveTrueOrderByProviderAsc())
                .thenReturn(List.of(paymentMethod));

        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        List<PaymentMethodDto> result = paymentMethodService.getActivePaymentMethods();

        assertThat(result).hasSize(1);

        verify(paymentMethodRepository).findAllByActiveTrueOrderByProviderAsc();
    }

    @Test
    void getActivePaymentMethods_empty() {
        when(paymentMethodRepository.findAllByActiveTrueOrderByProviderAsc()).thenReturn(List.of());

        List<PaymentMethodDto> result = paymentMethodService.getActivePaymentMethods();

        assertThat(result).isEmpty();
    }

    @Test
    void getPaymentMethodById_success() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        PaymentMethodDto result = paymentMethodService.getPaymentMethodById(1L);

        assertThat(result).isSameAs(paymentMethodDto);
    }

    @Test
    void getPaymentMethodById_notFound_throws() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentMethodService.getPaymentMethodById(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updatePaymentMethod_success() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "VISA",
                        "****1234",
                        "Updated details",
                        false
                );

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(paymentMethod)).thenReturn(paymentMethod);
        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        PaymentMethodDto result = paymentMethodService.updatePaymentMethod(1L, newDto);

        assertThat(result).isNotNull();
        assertThat(paymentMethod.getDetails()).isEqualTo("Updated details");
        assertThat(paymentMethod.isActive()).isFalse();

        verify(paymentMethodRepository, never()).existsPaymentMethodByProviderAndMaskedNumber(anyString(), any());
        verify(paymentMethodRepository).save(paymentMethod);
    }

    @Test
    void updatePaymentMethod_changedProvider_success() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "Mastercard",
                        "****1234",
                        "Updated",
                        true
                );

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.save(paymentMethod)).thenReturn(paymentMethod);
        when(modelMapper.map(paymentMethod, PaymentMethodDto.class)).thenReturn(paymentMethodDto);

        paymentMethodService.updatePaymentMethod(1L, newDto);

        verify(paymentMethodRepository).existsPaymentMethodByProviderAndMaskedNumber("Mastercard", "****1234");

        assertThat(paymentMethod.getProvider()).isEqualTo("Mastercard");
    }

    @Test
    void updatePaymentMethod_duplicate_throws() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "Mastercard",
                        "****1234",
                        "Updated",
                        true
                );

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentMethodRepository.existsPaymentMethodByProviderAndMaskedNumber("Mastercard", "****1234")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> paymentMethodService.updatePaymentMethod(1L, newDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(paymentMethodRepository, never()).save(any(PaymentMethod.class));
    }

    @Test
    void updatePaymentMethod_notFound_throws() {
        NewPaymentMethodDto newDto =
                new NewPaymentMethodDto(
                        "VISA",
                        "****1234",
                        "Updated",
                        true
                );

        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.updatePaymentMethod(1L, newDto)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deletePaymentMethod_success() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));

        paymentMethodService.deletePaymentMethod(1L);

        verify(paymentRepository).existsByPaymentMethod_Id(1L);
        verify(paymentMethodRepository).delete(paymentMethod);
        verify(paymentMethodRepository).flush();
    }

    @Test
    void deletePaymentMethod_inUse_throws() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.of(paymentMethod));
        when(paymentRepository.existsByPaymentMethod_Id(1L)).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> paymentMethodService.deletePaymentMethod(1L), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.PAYMENT_METHOD_IN_USE.getDescription());

        verify(paymentMethodRepository, never()).delete(any(PaymentMethod.class));
        verify(paymentMethodRepository, never()).flush();
    }

    @Test
    void deletePaymentMethod_notFound_throws() {
        when(paymentMethodRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.deletePaymentMethod(1L)).isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(paymentRepository);
        verify(paymentMethodRepository, never()).delete(any(PaymentMethod.class));
    }
}
