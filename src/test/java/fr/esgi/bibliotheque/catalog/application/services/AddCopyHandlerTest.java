package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.domain.*;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddCopyHandlerTest {

    @Mock WorkRepository workRepository;
    @Mock CopyRepository copyRepository;
    @Mock DomainIdGenerator idGenerator;
    AddCopyHandler handler;
    Work existingWork;

    @BeforeEach
    void setUp() {
        handler = new AddCopyHandler(workRepository, copyRepository, idGenerator);
        existingWork = Work.create(new WorkId("work-1"), "9782070360024", "Les Misérables",
            "Victor Hugo", "Gallimard", 1862, "Roman", "fr", null);
    }

    @Test
    void shouldAddCopyToExistingWork() {
        when(workRepository.findById(new WorkId("work-1"))).thenReturn(Optional.of(existingWork));
        when(copyRepository.existsByBarcode("BC12345678")).thenReturn(false);
        when(idGenerator.generate()).thenReturn("copy-uuid-1");
        when(copyRepository.save(any(Copy.class))).thenAnswer(inv -> inv.getArgument(0));

        CopyId result = handler.handle(new WorkId("work-1"), new AddCopyRequest("BC12345678", "campus-a", "A1", "Bon état"));

        assertThat(result.value()).isEqualTo("copy-uuid-1");
        verify(copyRepository).save(any(Copy.class));
    }

    @Test
    void shouldThrowWhenWorkNotFound() {
        when(workRepository.findById(new WorkId("unknown"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new WorkId("unknown"), new AddCopyRequest("BC12345678", "campus-a", null, null)))
            .isInstanceOf(ResourceNotFoundException.class);

        verify(copyRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExists() {
        when(workRepository.findById(new WorkId("work-1"))).thenReturn(Optional.of(existingWork));
        when(copyRepository.existsByBarcode("BC12345678")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(new WorkId("work-1"), new AddCopyRequest("BC12345678", "campus-a", null, null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("BC12345678");
    }
}
