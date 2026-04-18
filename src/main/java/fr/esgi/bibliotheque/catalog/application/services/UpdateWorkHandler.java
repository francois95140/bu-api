package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.UpdateWorkRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.UpdateWork;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateWorkHandler implements UpdateWork {

    private final WorkRepository workRepository;

    public UpdateWorkHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public void handle(WorkId id, UpdateWorkRequest request) {
        var work = workRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + id.value()));
        work.update(request.title(), request.authors(), request.publisher(),
            request.year(), request.subject(), request.language(), request.description());
        workRepository.save(work);
    }
}
