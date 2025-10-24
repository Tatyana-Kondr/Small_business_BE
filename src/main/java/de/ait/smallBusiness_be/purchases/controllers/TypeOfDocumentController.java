package de.ait.smallBusiness_be.purchases.controllers;

import de.ait.smallBusiness_be.purchases.controllers.api.TypeOfDocumentApi;
import de.ait.smallBusiness_be.purchases.dto.NewTypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.dto.TypeOfDocumentDto;
import de.ait.smallBusiness_be.purchases.services.TypeOfDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TypeOfDocumentController implements TypeOfDocumentApi {

    private final TypeOfDocumentService typeOfDocumentService;


    @Override
    public TypeOfDocumentDto addTypeOfDocument(NewTypeOfDocumentDto newType) {
        return typeOfDocumentService.createTypeOfDocument(newType);
    }

    @Override
    public List<TypeOfDocumentDto> getAllTypesOfDocument() {
        return typeOfDocumentService.findAllTypeOfDocument();
    }

    @Override
    public TypeOfDocumentDto getTypeOfDocumentById(Long id) {
        return typeOfDocumentService.getTypeOfDocumentById(id);
    }

    @Override
    public TypeOfDocumentDto updateTypeOfDocument(Long id, NewTypeOfDocumentDto newType) {
        return typeOfDocumentService.updateTypeOfDocument(id, newType);
    }

    @Override
    public void deleteTypeOfDocument(Long id) {
        typeOfDocumentService.deleteTypeOfDocument(id);
    }
}
