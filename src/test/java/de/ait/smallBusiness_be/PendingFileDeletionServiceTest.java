package de.ait.smallBusiness_be;

import de.ait.smallBusiness_be.purchases.dao.PendingFileDeletionRepository;
import de.ait.smallBusiness_be.purchases.model.PendingFileDeletion;

import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionProcessor;
import de.ait.smallBusiness_be.purchases.services.PendingFileDeletionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingFileDeletionServiceTest {

    @Mock
    private PendingFileDeletionRepository repository;

    @Mock
    private PendingFileDeletionProcessor processor;

    @InjectMocks
    private PendingFileDeletionService service;

    private PendingFileDeletion task1;
    private PendingFileDeletion task2;

    @BeforeEach
    void setUp() {

        task1 = PendingFileDeletion.builder()
                .id(1L)
                .purchaseId(10L)
                .fileUrl("document1.pdf")
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();

        task2 = PendingFileDeletion.builder()
                .id(2L)
                .purchaseId(10L)
                .fileUrl("document2.pdf")
                .createdAt(LocalDateTime.now())
                .attempts(0)
                .build();
    }

    @Test
    void createTasks_createsTasksForAllFiles() {

        List<String> fileUrls = List.of(
                "document1.pdf",
                "document2.pdf"
        );

        service.createTasks(10L, fileUrls);

        ArgumentCaptor<PendingFileDeletion> captor = ArgumentCaptor.forClass(PendingFileDeletion.class);

        verify(repository, times(2)).save(captor.capture());

        List<PendingFileDeletion> savedTasks = captor.getAllValues();

        assertThat(savedTasks).hasSize(2);

        assertThat(savedTasks)
                .extracting(PendingFileDeletion::getFileUrl)
                .containsExactly(
                        "document1.pdf",
                        "document2.pdf"
                );

        assertThat(savedTasks)
                .allSatisfy(task -> {
                    assertThat(task.getPurchaseId()).isEqualTo(10L);
                    assertThat(task.getCreatedAt()).isNotNull();});

        verifyNoInteractions(processor);
    }

    @Test
    void createTasks_emptyList_doesNotSave() {

        service.createTasks(10L, List.of());
        verify(repository, never()).save(any(PendingFileDeletion.class));
        verifyNoInteractions(processor);
    }

    @Test
    void processPendingFiles_processesAllTasks() {

        when(repository.findAll()).thenReturn(List.of(task1, task2));

        service.processPendingFiles();

        verify(processor).processOne(1L);
        verify(processor).processOne(2L);
        verify(processor, times(2)).processOne(anyLong());
    }

    @Test
    void processPendingFiles_emptyQueue_doesNothing() {

        when(repository.findAll()).thenReturn(List.of());
        service.processPendingFiles();
        verifyNoInteractions(processor);
    }

    @Test
    void processPendingFiles_oneTaskFails_continuesProcessing() {

        when(repository.findAll()).thenReturn(List.of(task1, task2));
        doThrow(new RuntimeException("Database error")).when(processor).processOne(1L);

        service.processPendingFiles();

        verify(processor).processOne(1L);
        verify(processor).processOne(2L);
    }

    @Test
    void processPendingFiles_usesTaskIds() {

        when(repository.findAll()).thenReturn(List.of(task1, task2));

        service.processPendingFiles();

        verify(processor).processOne(task1.getId());
        verify(processor).processOne(task2.getId());
        verify(processor, never()).processOne(10L);
    }
}

