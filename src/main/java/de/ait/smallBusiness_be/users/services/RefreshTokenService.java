
package de.ait.smallBusiness_be.users.services;

import de.ait.smallBusiness_be.exceptions.ErrorDescription;
import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.users.dao.RefreshTokenRepository;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.model.User;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // Создаём отдельный refresh token для каждой сессии
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(
                        ErrorDescription.USER_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isExpired(RefreshToken token) {
        return !token.getExpiryDate().isAfter(Instant.now());
    }

    @Transactional
    public void delete(RefreshToken token) {
        refreshTokenRepository.delete(token);
    }

    // Обновляем только токен текущей сессии
    @Transactional
    public RefreshToken rotate(RefreshToken token) {

        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );

        return refreshTokenRepository.save(token);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        long deleted = refreshTokenRepository
                .deleteByExpiryDateBefore(Instant.now());

        if (deleted > 0) {
            log.info("Deleted {} expired refresh tokens", deleted);
        }
    }
}
