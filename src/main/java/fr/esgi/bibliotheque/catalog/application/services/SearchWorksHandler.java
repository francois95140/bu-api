package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.application.usecases.SearchWorks;
import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchWorksHandler implements SearchWorks {

    private final WorkRepository workRepository;

    public SearchWorksHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public List<Work> handle(WorkFilters filters) {
        return workRepository.findAll(filters);
    }
}
