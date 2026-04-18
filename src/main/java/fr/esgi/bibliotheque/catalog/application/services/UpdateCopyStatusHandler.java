package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.models.UpdateCopyStatusRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.UpdateCopyStatus;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCopyStatusHandler implements UpdateCopyStatus {

    private final CopyRepository copyRepository;

    public UpdateCopyStatusHandler(CopyRepository copyRepository) {
        this.copyRepository = copyRepository;
    }

    @Override
    public void handle(CopyId id, UpdateCopyStatusRequest request) {
        var copy = copyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Exemplaire introuvable : " + id.value()));
        copy.updateStatus(request.status());
        copyRepository.save(copy);
    }
}
