package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWorkHandlerTest {

    @Mock WorkRepository workRepository;
    @Mock DomainIdGenerator idGenerator;
    CreateWorkHandler handler;

    @BeforeEach
    void setUp() { handler = new CreateWorkHandler(workRepository, idGenerator); }

    @Test
    void shouldCreateWorkAndReturnId() {
        when(workRepository.existsByIsbn("9782070360024")).thenReturn(false);
        when(idGenerator.generate()).thenReturn("uuid-123");
        when(workRepository.save(any(Work.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkId result = handler.handle(new CreateWorkRequest(
            "9782070360024", "Les Misérables", "Victor Hugo", "Gallimard", 1862, "Roman", "fr", null));

        assertThat(result.value()).isEqualTo("uuid-123");
        verify(workRepository).save(any(Work.class));
    }

    @Test
    void shouldThrowWhenIsbnAlreadyExists() {
        when(workRepository.existsByIsbn("9782070360024")).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(new CreateWorkRequest(
            "9782070360024", "Les Misérables", "Victor Hugo", null, null, null, null, null)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("9782070360024");

        verify(workRepository, never()).save(any());
    }
}
