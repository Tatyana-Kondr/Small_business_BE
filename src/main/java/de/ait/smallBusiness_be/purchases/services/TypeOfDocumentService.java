package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;

import java.util.List;


public interface TypeOfDocumentService {
    TypeOfDocumentDto createTypeOfDocument(NewTypeOfDocumentDto newTypeOfDocument);
    List<TypeOfDocumentDto> findAllTypeOfDocument();
    TypeOfDocumentDto getTypeOfDocumentById(Long id);
    TypeOfDocumentDto updateTypeOfDocument(Long id, NewTypeOfDocumentDto newTypeOfDocument);
    void deleteTypeOfDocument(Long id);
}
