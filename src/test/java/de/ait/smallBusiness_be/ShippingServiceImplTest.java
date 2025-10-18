package de.ait.smallBusiness_be;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.ait.smallBusiness_be.exceptions.RestApiException;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @Mock
    private ShippingRepository shippingRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ShippingServiceImpl shippingService;

    private Shipping shipping;
    private ShippingDto shippingDto;
    private NewShippingDto newShippingDto;

    @BeforeEach
    void setUp() {
        shipping = Shipping.builder().id(1L).name("DHL").build();
        shippingDto = new ShippingDto();
        shippingDto.setId(1L);
        shippingDto.setName("DHL");

        newShippingDto = new NewShippingDto();
        newShippingDto.setName("DHL");

        // lenient mapping для всех вызовов ModelMapper
        lenient().when(modelMapper.map(any(Shipping.class), eq(ShippingDto.class)))
                .thenReturn(shippingDto);
    }

    @Test
    void createShipping_success() {
        when(shippingRepository.existsByName(newShippingDto.getName())).thenReturn(false);
        when(shippingRepository.save(any(Shipping.class))).thenReturn(shipping);

        ShippingDto result = shippingService.createShipping(newShippingDto);

        assertNotNull(result);
        assertEquals("DHL", result.getName());
        verify(shippingRepository).save(any(Shipping.class));
    }

    @Test
    void createShipping_nameAlreadyExists_throwsException() {
        when(shippingRepository.existsByName(newShippingDto.getName())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> shippingService.createShipping(newShippingDto));

        assertEquals("Shipping with name 'DHL' already exists", exception.getMessage());
    }

    @Test
    void findAllShipping_success() {
        when(shippingRepository.findAll()).thenReturn(List.of(shipping));

        List<ShippingDto> result = shippingService.findAllShipping();

        assertEquals(1, result.size());
        assertEquals("DHL", result.get(0).getName());
    }

    @Test
    void findAllShipping_emptyList_throwsException() {
        when(shippingRepository.findAll()).thenReturn(Collections.emptyList());

        RestApiException exception = assertThrows(RestApiException.class,
                () -> shippingService.findAllShipping());

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getShippingById_success() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));

        ShippingDto result = shippingService.getShippingById(1L);

        assertNotNull(result);
        assertEquals("DHL", result.getName());
    }

    @Test
    void getShippingById_notFound_throwsException() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> shippingService.getShippingById(1L));
    }

    @Test
    void updateShipping_success() {
        NewShippingDto updateDto = new NewShippingDto();
        updateDto.setName("FedEx");

        Shipping updatedShipping = Shipping.builder().id(1L).name("FedEx").build();
        ShippingDto updatedDto = new ShippingDto();
        updatedDto.setId(1L);
        updatedDto.setName("FedEx");

        when(shippingRepository.findById(1L)).thenReturn(Optional.of(shipping));
        when(shippingRepository.save(any(Shipping.class))).thenReturn(updatedShipping);
        when(modelMapper.map(updatedShipping, ShippingDto.class)).thenReturn(updatedDto);

        ShippingDto result = shippingService.updateShipping(1L, updateDto);

        assertEquals("FedEx", result.getName());
        verify(shippingRepository).save(any(Shipping.class));
    }

    @Test
    void updateShipping_notFound_throwsException() {
        when(shippingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> shippingService.updateShipping(1L, newShippingDto));
    }

    @Test
    void deleteShipping_success() {
        when(shippingRepository.existsById(1L)).thenReturn(false);

        shippingService.deleteShipping(1L);

        verify(shippingRepository).deleteById(1L);
    }

    @Test
    void deleteShipping_notFound_throwsException() {
        when(shippingRepository.existsById(1L)).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> shippingService.deleteShipping(1L));
    }
}
