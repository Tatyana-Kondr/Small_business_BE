package de.ait.smallBusiness_be.purchases.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingFileDeletionScheduler {

    private final PendingFileDeletionService pendingFileDeletionService;

    @Scheduled(fixedDelay = 300000)
    public void retryPendingDeletions() {
        pendingFileDeletionService.processPendingFiles();
    }
}
