
package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.exceptions.RestApiException;
import de.ait.smallBusiness_be.users.dao.RefreshTokenRepository;
import de.ait.smallBusiness_be.users.dao.UserRepository;
import de.ait.smallBusiness_be.users.model.RefreshToken;
import de.ait.smallBusiness_be.users.model.User;
import de.ait.smallBusiness_be.users.services.RefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
                .id(10L)
                .user(user)
                .token("oldToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
    }

    // -------------------- CREATE --------------------

    @Test
    void createRefreshToken_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();

        RefreshToken result =
                refreshTokenService.createRefreshToken(1L);

        Instant after = Instant.now();

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertEquals(user, result.getUser());
        assertNull(result.getId());

        assertFalse(result.getExpiryDate()
                .isBefore(before.plus(7, ChronoUnit.DAYS)));

        assertFalse(result.getExpiryDate()
                .isAfter(after.plus(7, ChronoUnit.DAYS)));

        verify(refreshTokenRepository)
                .save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_multipleSessions_success() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken first =
                refreshTokenService.createRefreshToken(1L);

        RefreshToken second =
                refreshTokenService.createRefreshToken(1L);

        assertNotSame(first, second);

        assertNotEquals(
                first.getToken(),
                second.getToken()
        );

        assertEquals(user, first.getUser());
        assertEquals(user, second.getUser());

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_userNotFound_throwsException() {

        when(userRepository.findById(99L))
                .thenReturn(Optional.empty());

        RestApiException exception = assertThrows(
                RestApiException.class,
                () -> refreshTokenService.createRefreshToken(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatus()
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    // -------------------- FIND --------------------

    @Test
    void findByToken_success() {

        when(refreshTokenRepository.findByToken("oldToken"))
                .thenReturn(Optional.of(token));

        Optional<RefreshToken> result =
                refreshTokenService.findByToken("oldToken");

        assertTrue(result.isPresent());
        assertSame(token, result.get());
    }

    @Test
    void findByToken_notFound() {

        when(refreshTokenRepository.findByToken("unknown"))
                .thenReturn(Optional.empty());

        Optional<RefreshToken> result =
                refreshTokenService.findByToken("unknown");

        assertTrue(result.isEmpty());
    }

    // -------------------- EXPIRATION --------------------

    @Test
    void isExpired_true() {

        token.setExpiryDate(
                Instant.now().minusSeconds(10)
        );

        assertTrue(refreshTokenService.isExpired(token));
    }

    @Test
    void isExpired_false() {

        token.setExpiryDate(
                Instant.now().plusSeconds(10)
        );

        assertFalse(refreshTokenService.isExpired(token));
    }

    // -------------------- ROTATE --------------------

    @Test
    void rotate_success() {

        String oldToken = token.getToken();
        Instant oldExpiryDate = token.getExpiryDate();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken result =
                refreshTokenService.rotate(token);

        assertSame(token, result);
        assertEquals(10L, result.getId());
        assertEquals(user, result.getUser());

        assertNotEquals(oldToken, result.getToken());

        assertTrue(
                result.getExpiryDate().isAfter(oldExpiryDate)
        );

        verify(refreshTokenRepository)
                .save(token);
    }

    @Test
    void rotate_doesNotModifyOtherSession() {

        RefreshToken secondSession = RefreshToken.builder()
                .id(11L)
                .user(user)
                .token("secondToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        String secondTokenBefore =
                secondSession.getToken();

        Instant secondExpiryBefore =
                secondSession.getExpiryDate();

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        refreshTokenService.rotate(token);

        assertEquals(
                secondTokenBefore,
                secondSession.getToken()
        );

        assertEquals(
                secondExpiryBefore,
                secondSession.getExpiryDate()
        );

        verify(refreshTokenRepository)
                .save(token);

        verify(refreshTokenRepository, never())
                .save(secondSession);
    }

    // -------------------- DELETE --------------------

    @Test
    void delete_success() {

        refreshTokenService.delete(token);

        verify(refreshTokenRepository)
                .delete(token);
    }

    @Test
    void delete_onlySelectedSession() {

        RefreshToken secondSession = RefreshToken.builder()
                .id(11L)
                .user(user)
                .token("secondToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();

        refreshTokenService.delete(token);

        verify(refreshTokenRepository)
                .delete(token);

        verify(refreshTokenRepository, never())
                .delete(secondSession);

        verify(refreshTokenRepository, times(1))
                .delete(any(RefreshToken.class));
    }

    // -------------------- CLEANUP --------------------

    @Test
    void cleanupExpiredTokens_success() {

        when(refreshTokenRepository.deleteByExpiryDateBefore(
                any(Instant.class)
        )).thenReturn(3L);

        refreshTokenService.cleanupExpiredTokens();

        ArgumentCaptor<Instant> captor =
                ArgumentCaptor.forClass(Instant.class);

        verify(refreshTokenRepository)
                .deleteByExpiryDateBefore(captor.capture());

        assertNotNull(captor.getValue());
    }

    @Test
    void cleanupExpiredTokens_noExpiredTokens() {

        when(refreshTokenRepository.deleteByExpiryDateBefore(
                any(Instant.class)
        )).thenReturn(0L);

        refreshTokenService.cleanupExpiredTokens();

        verify(refreshTokenRepository)
                .deleteByExpiryDateBefore(any(Instant.class));
    }
}
