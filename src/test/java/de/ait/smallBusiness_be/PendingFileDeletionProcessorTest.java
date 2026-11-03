package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.purchases.dao.PendingFileDeletionRepository;
import de.ait.smallBusiness_be.purchases.model.PendingFileDeletion;

import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingFileDeletionProcessorTest {

    @Mock
    private PendingFileDeletionRepository repository;

    @InjectMocks
    private PendingFileDeletionProcessor processor;

    @TempDir
    Path tempDir;

    private PendingFileDeletion task;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                processor,
                "documentDir",
                tempDir.toString()
        );

        task = PendingFileDeletion.builder()
                .id(1L)
                .purchaseId(10L)
                .fileUrl("test-document.pdf")
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();
    }

    @Test
    void processOne_taskNotFound() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        processor.processOne(1L);

        verify(repository).findById(1L);
        verify(repository, never())
                .delete(any(PendingFileDeletion.class));
        verify(repository, never())
                .save(any(PendingFileDeletion.class));
    }

    @Test
    void processOne_existingFile_deletesFileAndTask()
            throws IOException {

        Path file = tempDir.resolve("test-document.pdf");
        Files.writeString(file, "Test content");

        assertThat(Files.exists(file)).isTrue();

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        processor.processOne(1L);

        assertThat(Files.exists(file)).isFalse();

        verify(repository).delete(task);
        verify(repository, never())
                .save(any(PendingFileDeletion.class));
    }

    @Test
    void processOne_fileAlreadyMissing_deletesTask() {

        Path file = tempDir.resolve("test-document.pdf");

        assertThat(Files.exists(file)).isFalse();

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        processor.processOne(1L);

        verify(repository).delete(task);
        verify(repository, never())
                .save(any(PendingFileDeletion.class));
    }

    @Test
    void processOne_deletionFails_incrementsAttempts()
            throws IOException {

        // Создаём непустую папку вместо файла.
        // Files.deleteIfExists() не сможет удалить
        // непустую папку и выбросит IOException.

        Path directory = tempDir.resolve("test-document.pdf");
        Files.createDirectory(directory);

        Files.writeString(
                directory.resolve("child.txt"),
                "Prevent directory deletion"
        );

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        processor.processOne(1L);

        assertThat(task.getAttempts()).isEqualTo(1);
        assertThat(task.getLastAttemptAt()).isNotNull();
        assertThat(task.getLastError()).isNotBlank();

        assertThat(Files.exists(directory)).isTrue();

        verify(repository).save(task);
        verify(repository, never())
                .delete(any(PendingFileDeletion.class));
    }

    @Test
    void processOne_retryAfterFailure_deletesFileAndTask()
            throws IOException {

        Path directory = tempDir.resolve("test-document.pdf");
        Files.createDirectory(directory);

        Path child = directory.resolve("child.txt");
        Files.writeString(child, "Prevent deletion");

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        // Первая попытка: ошибка удаления
        processor.processOne(1L);

        assertThat(task.getAttempts()).isEqualTo(1);
        assertThat(task.getLastError()).isNotBlank();

        verify(repository).save(task);

        // Устраняем причину ошибки
        Files.delete(child);
        Files.delete(directory);

        // Создаём обычный файл с тем же именем
        Path file = tempDir.resolve("test-document.pdf");
        Files.writeString(file, "Retry test");

        // Вторая попытка
        processor.processOne(1L);

        assertThat(Files.exists(file)).isFalse();

        verify(repository).delete(task);
        verify(repository, times(1)).save(task);
        verify(repository, times(2)).findById(1L);
    }
}

