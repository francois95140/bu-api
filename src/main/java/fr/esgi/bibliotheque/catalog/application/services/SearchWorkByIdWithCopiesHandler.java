package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.usecases.SearchWorkByIdWithCopies;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SearchWorkByIdWithCopiesHandler implements SearchWorkByIdWithCopies {

    private final WorkRepository workRepository;

    public SearchWorkByIdWithCopiesHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public Optional<Work> handle(WorkId id) {
        return workRepository.findByIdWithCopies(id);
    }
}
