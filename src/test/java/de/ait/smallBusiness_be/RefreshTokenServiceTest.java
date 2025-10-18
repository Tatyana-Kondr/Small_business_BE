package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.users.dao.RefreshTokenRepository;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.model.User;
import de.ait.smallBusiness_be.users.services.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;
    private RefreshToken token;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        token = RefreshToken.builder()
                .user(user)
                .token("oldToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void createRefreshToken_newToken_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void createRefreshToken_existingToken_updatesToken() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUser(user)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertNotEquals("oldToken", result.getToken());
        assertEquals(user, result.getUser());
        assertTrue(result.getExpiryDate().isAfter(Instant.now()));
    }

    @Test
    void findByToken_success() {
        when(refreshTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByToken("token123");
        assertTrue(result.isPresent());
        assertEquals(token, result.get());
    }

    @Test
    void isExpired_true() {
        token.setExpiryDate(Instant.now().minusSeconds(10));
        assertTrue(refreshTokenService.isExpired(token));
    }

    @Test
    void isExpired_false() {
        token.setExpiryDate(Instant.now().plusSeconds(10));
        assertFalse(refreshTokenService.isExpired(token));
    }

    @Test
    void delete_success() {
        doNothing().when(refreshTokenRepository).delete(token);
        refreshTokenService.delete(token);
        verify(refreshTokenRepository).delete(token);
    }
}
