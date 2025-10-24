package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.TypeOfDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeOfDocumentRepository extends JpaRepository<TypeOfDocument, Long> {
    boolean existsByName(String name);
    Optional<TypeOfDocument> findByName(String name);
}
