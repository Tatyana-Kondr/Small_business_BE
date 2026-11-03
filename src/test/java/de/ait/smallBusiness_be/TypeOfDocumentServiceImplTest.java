
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import de.ait.smallBusiness_be.purchases.services.TypeOfDocumentServiceImpl;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeOfDocumentServiceImplTest {

    @Mock
    private TypeOfDocumentRepository typeOfDocumentRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TypeOfDocumentServiceImpl service;

    private TypeOfDocument document;
    private TypeOfDocumentDto documentDto;
    private NewTypeOfDocumentDto newDto;

    @BeforeEach
    void setUp() {
        document = TypeOfDocument.builder()
                .name("RECHNUNG")
                .build();

        documentDto = new TypeOfDocumentDto();
        newDto = new NewTypeOfDocumentDto();
        newDto.setName("RECHNUNG");
    }

    @Test
    void createTypeOfDocument_success() {
        when(typeOfDocumentRepository.save(any(TypeOfDocument.class))).thenReturn(document);
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);

        TypeOfDocumentDto result = service.createTypeOfDocument(newDto);

        assertThat(result).isSameAs(documentDto);

        verify(typeOfDocumentRepository).existsByName("RECHNUNG");
        verify(typeOfDocumentRepository).save(any(TypeOfDocument.class));
    }

    @Test
    void createTypeOfDocument_trimsName() {
        newDto.setName("  RECHNUNG  ");

        when(typeOfDocumentRepository.save(any(TypeOfDocument.class))).thenReturn(document);
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);
        service.createTypeOfDocument(newDto);
        ArgumentCaptor<TypeOfDocument> captor = ArgumentCaptor.forClass(TypeOfDocument.class);
        verify(typeOfDocumentRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("RECHNUNG");
        verify(typeOfDocumentRepository).existsByName("RECHNUNG");
    }

    @Test
    void createTypeOfDocument_duplicate_throwsConflict() {
        when(typeOfDocumentRepository.existsByName("RECHNUNG")).thenReturn(true);
        RestApiException exception = assertThrows(RestApiException.class, () -> service.createTypeOfDocument(newDto));
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(typeOfDocumentRepository, never()).save(any(TypeOfDocument.class));
    }

    @Test
    void createTypeOfDocument_nullName_throws() {
        newDto.setName(null);

        assertThrows(NullPointerException.class, () -> service.createTypeOfDocument(newDto));
        verifyNoInteractions(typeOfDocumentRepository);
    }

    @Test
    void findAllTypeOfDocument_success() {
        when(typeOfDocumentRepository.findAll()).thenReturn(List.of(document));
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);

        List<TypeOfDocumentDto> result = service.findAllTypeOfDocument();

        assertThat(result).hasSize(1).containsExactly(documentDto);
        verify(typeOfDocumentRepository).findAll();
    }

    @Test
    void findAllTypeOfDocument_empty_returnsEmptyList() {
        when(typeOfDocumentRepository.findAll()).thenReturn(List.of());
        List<TypeOfDocumentDto> result = service.findAllTypeOfDocument();
        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    void getTypeOfDocumentById_success() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);

        TypeOfDocumentDto result = service.getTypeOfDocumentById(1L);

        assertThat(result).isSameAs(documentDto);
        verify(typeOfDocumentRepository).findById(1L);
    }

    @Test
    void getTypeOfDocumentById_notFound_throws() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.getTypeOfDocumentById(1L));
        verifyNoInteractions(modelMapper);
    }

    @Test
    void updateTypeOfDocument_success() {
        NewTypeOfDocumentDto updateDto = new NewTypeOfDocumentDto();
        updateDto.setName("BON");

        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(typeOfDocumentRepository.save(document)).thenReturn(document);
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);

        TypeOfDocumentDto result = service.updateTypeOfDocument(1L, updateDto);

        assertThat(result).isSameAs(documentDto);
        assertThat(document.getName()).isEqualTo("BON");

        verify(typeOfDocumentRepository).existsByName("BON");
        verify(typeOfDocumentRepository).save(document);
    }

    @Test
    void updateTypeOfDocument_sameName_success() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(typeOfDocumentRepository.save(document)).thenReturn(document);
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);

        TypeOfDocumentDto result = service.updateTypeOfDocument(1L, newDto);

        assertThat(result).isSameAs(documentDto);
        verify(typeOfDocumentRepository, never()).existsByName(anyString());
        verify(typeOfDocumentRepository).save(document);
    }

    @Test
    void updateTypeOfDocument_trimsName() {
        NewTypeOfDocumentDto updateDto = new NewTypeOfDocumentDto();
        updateDto.setName("  BON  ");

        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(typeOfDocumentRepository.save(document)).thenReturn(document);
        when(modelMapper.map(document, TypeOfDocumentDto.class)).thenReturn(documentDto);
        service.updateTypeOfDocument(1L, updateDto);
        assertThat(document.getName()).isEqualTo("BON");
        verify(typeOfDocumentRepository).existsByName("BON");
    }

    @Test
    void updateTypeOfDocument_duplicate_throwsConflict() {
        NewTypeOfDocumentDto updateDto = new NewTypeOfDocumentDto();
        updateDto.setName("BON");

        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(typeOfDocumentRepository.existsByName("BON")).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.updateTypeOfDocument(1L, updateDto));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(document.getName()).isEqualTo("RECHNUNG");
        verify(typeOfDocumentRepository, never()).save(any(TypeOfDocument.class));
    }

    @Test
    void updateTypeOfDocument_notFound_throws() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.updateTypeOfDocument(1L, newDto));
        verify(typeOfDocumentRepository, never()).save(any(TypeOfDocument.class));
    }

    @Test
    void deleteTypeOfDocument_success() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));

        service.deleteTypeOfDocument(1L);

        verify(purchaseRepository).existsByDocument_Id(1L);
        verify(paymentRepository).existsByDocument_Id(1L);
        verify(typeOfDocumentRepository).delete(document);
        verify(typeOfDocumentRepository).flush();
    }

    @Test
    void deleteTypeOfDocument_usedInPurchases_throwsConflict() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(purchaseRepository.existsByDocument_Id(1L)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteTypeOfDocument(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verifyNoInteractions(paymentRepository);
        verify(typeOfDocumentRepository, never()).delete(any(TypeOfDocument.class));
        verify(typeOfDocumentRepository, never()).flush();
    }

    @Test
    void deleteTypeOfDocument_usedInPayments_throwsConflict() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(paymentRepository.existsByDocument_Id(1L)).thenReturn(true);

        RestApiException exception = assertThrows(RestApiException.class, () -> service.deleteTypeOfDocument(1L));

        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT);

        verify(purchaseRepository).existsByDocument_Id(1L);
        verify(paymentRepository).existsByDocument_Id(1L);
        verify(typeOfDocumentRepository, never()).delete(any(TypeOfDocument.class));
        verify(typeOfDocumentRepository, never()).flush();
    }

    @Test
    void deleteTypeOfDocument_notFound_throws() {
        when(typeOfDocumentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> service.deleteTypeOfDocument(1L));
        verifyNoInteractions(purchaseRepository, paymentRepository);
        verify(typeOfDocumentRepository, never()).delete(any(TypeOfDocument.class));
    }
}

