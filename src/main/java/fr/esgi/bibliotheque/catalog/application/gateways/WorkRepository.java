package fr.esgi.bibliotheque.catalog.application.gateways;

import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import java.util.List;
import java.util.Optional;

public interface WorkRepository {
    Work save(Work work);
    Optional<Work> findById(WorkId id);
    Optional<Work> findByIdWithCopies(WorkId id);
    List<Work> findAll(WorkFilters filters);
    void deleteById(WorkId id);
    boolean existsByIsbn(String isbn);
}
