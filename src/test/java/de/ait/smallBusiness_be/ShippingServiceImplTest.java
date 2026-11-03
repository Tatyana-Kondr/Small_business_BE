
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.sales.dao.SaleRepository;
import de.ait.smallBusiness_be.sales.dao.ShippingRepository;
import de.ait.smallBusiness_be.sales.dto.NewShippingDto;
import de.ait.smallBusiness_be.sales.dto.ShippingDto;
import de.ait.smallBusiness_be.sales.models.Shipping;
import de.ait.smallBusiness_be.sales.services.impl.ShippingServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    private Shipping shipping;
    private ShippingDto shippingDto;
    private NewShippingDto newShippingDto;

    @BeforeEach
    void setUp() {
        shipping = Shipping.builder()
                .id(1L)
                .name("DHL")
                .build();

        shippingDto = new ShippingDto();
        shippingDto.setId(1L);
        shippingDto.setName("DHL");

        newShippingDto = new NewShippingDto();
        newShippingDto.setName("DHL");
    }

    @Test
    void createShipping_success() {
        when(shippingRepository.save(any(Shipping.class))).thenReturn(shipping);
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(shippingDto);

        ShippingDto result = shippingService.createShipping(newShippingDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("DHL");

        verify(shippingRepository).existsByName("DHL");
        verify(shippingRepository).save(argThat(entity -> entity.getName().equals("DHL")));
    }

    @Test
    void createShipping_trimsName() {
        newShippingDto.setName("  DHL  ");

        when(shippingRepository.save(any(Shipping.class))).thenReturn(shipping);
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(shippingDto);

        shippingService.createShipping(newShippingDto);

        verify(shippingRepository).existsByName("DHL");
        verify(shippingRepository).save(argThat(entity -> entity.getName().equals("DHL")));
    }

    @Test
    void createShipping_nameAlreadyExists_throwsException() {
        when(shippingRepository.existsByName("DHL")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> shippingService.createShipping(newShippingDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.SHIPPING_ALREADY_EXISTS.getDescription());

        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void findAllShipping_success() {
        when(shippingRepository.findAll()).thenReturn(List.of(shipping));
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(shippingDto);

        List<ShippingDto> result = shippingService.findAllShipping();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("DHL");

        verify(shippingRepository).findAll();
    }

    @Test
    void findAllShipping_empty_returnsEmptyList() {
        when(shippingRepository.findAll()).thenReturn(List.of());

        List<ShippingDto> result = shippingService.findAllShipping();

        assertThat(result).isEmpty();

        verify(shippingRepository).findAll();
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getShippingById_success() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(shippingDto);

        ShippingDto result = shippingService.getShippingById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("DHL");
    }

    @Test
    void getShippingById_notFound_throwsException() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.getShippingById(1L)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(ErrorDescription.SHIPPING_NOT_FOUND.getDescription());
    }

    @Test
    void updateShipping_success() {
        NewShippingDto updateDto = new NewShippingDto();
        updateDto.setName("FedEx");

        ShippingDto updatedDto = new ShippingDto();
        updatedDto.setId(1L);
        updatedDto.setName("FedEx");

        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(updatedDto);

        ShippingDto result = shippingService.updateShipping(1L, updateDto);

        assertThat(result.getName()).isEqualTo("FedEx");
        assertThat(shipping.getName()).isEqualTo("FedEx");

        verify(shippingRepository).existsByName("FedEx");
        verify(shippingRepository).save(shipping);
    }

    @Test
    void updateShipping_sameName_success() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(shipping)).thenReturn(shipping);
        when(modelMapper.map(shipping, ShippingDto.class)).thenReturn(shippingDto);

        ShippingDto result = shippingService.updateShipping(1L, newShippingDto);

        assertThat(result.getName()).isEqualTo("DHL");

        verify(shippingRepository, never()).existsByName(any());
        verify(shippingRepository).save(shipping);
    }

    @Test
    void updateShipping_duplicateName_throwsException() {
        NewShippingDto updateDto = new NewShippingDto();
        updateDto.setName("FedEx");

        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(shippingRepository.existsByName("FedEx")).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> shippingService.updateShipping(1L, updateDto), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.SHIPPING_ALREADY_EXISTS.getDescription());

        assertThat(shipping.getName()).isEqualTo("DHL");

        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void updateShipping_notFound_throwsException() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.updateShipping(1L, newShippingDto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(shippingRepository, never()).save(any(Shipping.class));
    }

    @Test
    void deleteShipping_success() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));

        shippingService.deleteShipping(1L);

        verify(saleRepository).existsByShipping_Id(1L);
        verify(shippingRepository).delete(shipping);
        verify(shippingRepository).flush();
    }

    @Test
    void deleteShipping_notFound_throwsException() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shippingService.deleteShipping(1L))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(saleRepository);
        verify(shippingRepository, never()).delete(any(Shipping.class));
    }

    @Test
    void deleteShipping_inUse_throwsConflict() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(saleRepository.existsByShipping_Id(1L)).thenReturn(true);

        RestApiException exception = catchThrowableOfType(() -> shippingService.deleteShipping(1L), RestApiException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessage()).contains(ErrorDescription.SHIPPING_IN_USE.getDescription());

        verify(shippingRepository, never()).delete(any(Shipping.class));
        verify(shippingRepository, never()).flush();
    }
}
