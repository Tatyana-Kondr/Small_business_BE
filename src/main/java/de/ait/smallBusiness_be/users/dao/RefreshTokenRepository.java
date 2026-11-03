package de.ait.smallBusiness_be.users.dao;

import de.ait.smallBusiness_be.users.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    long deleteByExpiryDateBefore(Instant now);
}
