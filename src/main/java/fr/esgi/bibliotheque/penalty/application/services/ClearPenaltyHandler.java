package fr.esgi.bibliotheque.penalty.application.services;

import fr.esgi.bibliotheque.penalty.application.gateways.PenaltyRepository;
import fr.esgi.bibliotheque.penalty.application.usecases.ClearPenalty;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClearPenaltyHandler implements ClearPenalty {

    private final PenaltyRepository penaltyRepository;
    private final TimeProvider timeProvider;

    public ClearPenaltyHandler(PenaltyRepository penaltyRepository, TimeProvider timeProvider) {
        this.penaltyRepository = penaltyRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle(PenaltyId id) {
        var penalty = penaltyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pénalité introuvable : " + id.value()));
        penalty.clear(timeProvider);
        penaltyRepository.save(penalty);
    }
}
