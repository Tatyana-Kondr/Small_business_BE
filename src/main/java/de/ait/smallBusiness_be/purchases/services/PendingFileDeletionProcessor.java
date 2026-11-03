package de.ait.smallBusiness_be.purchases.services;

import de.ait.smallBusiness_be.purchases.dao.PendingFileDeletionRepository;
import de.ait.smallBusiness_be.purchases.model.PendingFileDeletion;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PendingFileDeletionProcessor {

    private final PendingFileDeletionRepository repository;

    @Value("${file.document-dir}")
    private String documentDir;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processOne(Long taskId) {

        PendingFileDeletion task = repository.findById(taskId)
                .orElse(null);

        if (task == null) {
            return;
        }

        try {
            Path baseDir = Paths.get(documentDir)
                    .toAbsolutePath()
                    .normalize();

            String fileName = Paths.get(task.getFileUrl())
                    .getFileName()
                    .toString();

            Path filePath = baseDir.resolve(fileName).normalize();

            if (!filePath.startsWith(baseDir)) {
                throw new IOException("Invalid document path");
            }

            Files.deleteIfExists(filePath);

            repository.delete(task);

        } catch (IOException | SecurityException | java.nio.file.InvalidPathException e) {

            task.setAttempts(task.getAttempts() + 1);
            task.setLastAttemptAt(LocalDateTime.now());

            String message = e.getMessage() != null
                    ? e.getMessage()
                    : "File deletion failed";

            task.setLastError(
                    message.substring(0, Math.min(message.length(), 1000))
            );

            repository.save(task);
        }
    }
}
