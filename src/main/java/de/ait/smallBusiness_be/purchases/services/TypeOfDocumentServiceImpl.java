package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.payments.dao.PaymentRepository;
import de.ait.smallBusiness_be.purchases.dao.PurchaseRepository;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeOfDocumentServiceImpl implements TypeOfDocumentService {

    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final PurchaseRepository purchaseRepository;
    private final PaymentRepository paymentRepository;
    private final ModelMapper modelMapper;

    @Override
    public TypeOfDocumentDto createTypeOfDocument(NewTypeOfDocumentDto newTypeOfDocument) {

        String name = newTypeOfDocument.getName().trim();

        if (typeOfDocumentRepository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.TYPE_OF_DOCUMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        TypeOfDocument type = TypeOfDocument.builder().name(name).build();
        TypeOfDocument savedType = typeOfDocumentRepository.save(type);

        return modelMapper.map(savedType, TypeOfDocumentDto.class);
    }

    @Override
    public List<TypeOfDocumentDto> findAllTypeOfDocument() {
        return typeOfDocumentRepository.findAll()
                .stream()
                .map(typeOfDocument -> modelMapper.map(typeOfDocument, TypeOfDocumentDto.class))
                .toList();
    }

    @Override
    public TypeOfDocumentDto getTypeOfDocumentById(Long id) {
        TypeOfDocument typeOfDocument = typeOfDocumentRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));
        return modelMapper.map(typeOfDocument, TypeOfDocumentDto.class);
    }

    @Override
    public TypeOfDocumentDto updateTypeOfDocument(Long id, NewTypeOfDocumentDto newTypeOfDocument) {

        TypeOfDocument typeOfDocument = typeOfDocumentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));

        String name = newTypeOfDocument.getName().trim();

        if (!typeOfDocument.getName().equals(name) && typeOfDocumentRepository.existsByName(name)) {
            throw new RestApiException(ErrorDescription.TYPE_OF_DOCUMENT_ALREADY_EXISTS, HttpStatus.CONFLICT);
        }

        typeOfDocument.setName(name);
        TypeOfDocument savedType = typeOfDocumentRepository.save(typeOfDocument);

        return modelMapper.map(savedType, TypeOfDocumentDto.class);
    }

    @Override
    @Transactional
    public void deleteTypeOfDocument(Long id) {
        TypeOfDocument type = typeOfDocumentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorDescription.TYPE_OF_DOCUMENT_NOT_FOUND.getDescription()));

        if (purchaseRepository.existsByDocument_Id(id) || paymentRepository.existsByDocument_Id(id)) {
            throw new RestApiException(ErrorDescription.TYPE_OF_DOCUMENT_IN_USE, HttpStatus.CONFLICT);
        }

        typeOfDocumentRepository.delete(type);
        typeOfDocumentRepository.flush();
    }
}
