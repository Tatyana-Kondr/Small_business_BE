package de.ait.smallBusiness_be.purchases.dao;

import de.ait.smallBusiness_be.purchases.model.PendingFileDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingFileDeletionRepository
        extends JpaRepository<PendingFileDeletion, Long> {
}
