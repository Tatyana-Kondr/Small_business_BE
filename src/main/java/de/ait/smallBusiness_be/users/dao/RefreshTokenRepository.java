package de.ait.smallBusiness_be.users.dao;

import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);

}
