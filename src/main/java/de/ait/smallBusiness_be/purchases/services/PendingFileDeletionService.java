package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dao.PendingFileDeletionRepository;
import de.ait.smallBusiness_be.purchases.model.PendingFileDeletion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingFileDeletionService {

    private final PendingFileDeletionRepository repository;
    private final PendingFileDeletionProcessor processor;

    @Transactional
    public void createTasks(Long purchaseId, List<String> fileUrls) {

        for (String fileUrl : fileUrls) {

            PendingFileDeletion task = PendingFileDeletion.builder()
                    .purchaseId(purchaseId)
                    .fileUrl(fileUrl)
                    .createdAt(LocalDateTime.now())
                    .build();

            repository.save(task);
        }
    }

    public synchronized void processPendingFiles() {

        List<Long> taskIds = repository.findAll()
                .stream()
                .map(PendingFileDeletion::getId)
                .toList();

        for (Long taskId : taskIds) {
            try {
                processor.processOne(taskId);
            } catch (RuntimeException e) {
                // Ошибка БД не должна менять результат
                // уже успешно удалённой покупки.
                // Задание остаётся для следующей попытки.
            }
        }
    }
}