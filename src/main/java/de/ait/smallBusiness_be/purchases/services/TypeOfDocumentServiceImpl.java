package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.purchases.dao.TypeOfDocumentRepository;
import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TypeOfDocumentServiceImpl implements TypeOfDocumentService {

    private final TypeOfDocumentRepository typeOfDocumentRepository;
    private final ModelMapper modelMapper;

    @Override
    public TypeOfDocumentDto createTypeOfDocument(NewTypeOfDocumentDto newTypeOfDocument) {
        if (typeOfDocumentRepository.existsByName(newTypeOfDocument.getName())) {
            throw new IllegalArgumentException("Type Of Document with name '" + newTypeOfDocument.getName() + "' already exists");
        }
        TypeOfDocument type = TypeOfDocument.builder()
                .name(newTypeOfDocument.getName())
                .build();
        TypeOfDocument savedType = typeOfDocumentRepository.save(type);

        return modelMapper.map(savedType, TypeOfDocumentDto.class);
    }

    @Override
    public List<TypeOfDocumentDto> findAllTypeOfDocument() {
        List<TypeOfDocument> typeOfDocuments = typeOfDocumentRepository.findAll();
        if (typeOfDocuments.isEmpty()) {throw new RestApiException(ErrorDescription.LIST_IS_EMPTY, HttpStatus.NOT_FOUND);}
        return typeOfDocuments.stream().map(typeOfDocument -> modelMapper.map(typeOfDocument, TypeOfDocumentDto.class)).collect(Collectors.toList());
    }

    @Override
    public TypeOfDocumentDto getTypeOfDocumentById(Long id) {
        TypeOfDocument typeOfDocument = typeOfDocumentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Type Of Document not found"));
        return modelMapper.map(typeOfDocument, TypeOfDocumentDto.class);
    }

    @Override
    public TypeOfDocumentDto updateTypeOfDocument(Long id, NewTypeOfDocumentDto newTypeOfDocument) {
        TypeOfDocument typeOfDocument = typeOfDocumentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Type Of Document not found"));
        typeOfDocument.setName(newTypeOfDocument.getName());
        TypeOfDocument savedType = typeOfDocumentRepository.save(typeOfDocument);
        return modelMapper.map(savedType, TypeOfDocumentDto.class);
    }

    @Override
    public void deleteTypeOfDocument(Long id) {
        if (typeOfDocumentRepository.existsById(id)) {
            throw new EntityNotFoundException("Type Of Document with id '" + id + "' not found");
        }
        typeOfDocumentRepository.deleteById(id);
    }
}
