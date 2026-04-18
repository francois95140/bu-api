package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.usecases.DeleteWork;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteWorkHandler implements DeleteWork {

    private final WorkRepository workRepository;

    public DeleteWorkHandler(WorkRepository workRepository) {
        this.workRepository = workRepository;
    }

    @Override
    public void handle(WorkId id) {
        if (workRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Ouvrage introuvable : " + id.value());
        }
        workRepository.deleteById(id);
    }
}
