package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionScheduler;
import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingFileDeletionSchedulerTest {

    @Mock
    private PendingFileDeletionService pendingFileDeletionService;

    @InjectMocks
    private PendingFileDeletionScheduler scheduler;

    @Test
    void retryPendingDeletions_callsService() {

        scheduler.retryPendingDeletions();
        verify(pendingFileDeletionService, times(1)).processPendingFiles();
    }

    @Test
    void retryPendingDeletions_serviceThrowsException() {

        doThrow(new RuntimeException("Processing error"))
                .when(pendingFileDeletionService)
                .processPendingFiles();

        assertThrows(RuntimeException.class, () -> scheduler.retryPendingDeletions());
        verify(pendingFileDeletionService).processPendingFiles();
    }

    @Test
    void retryPendingDeletions_hasCorrectSchedule() throws NoSuchMethodException {

        Method method = PendingFileDeletionScheduler.class.getDeclaredMethod("retryPendingDeletions");

        Scheduled scheduled = method.getAnnotation(Scheduled.class);

        assertThat(scheduled).isNotNull();
        assertThat(scheduled.fixedDelay()).isEqualTo(300000L);
    }
}

