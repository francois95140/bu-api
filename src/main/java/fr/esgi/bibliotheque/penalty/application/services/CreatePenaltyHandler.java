package fr.esgi.bibliotheque.penalty.application.services;

import fr.esgi.bibliotheque.penalty.application.gateways.PenaltyRepository;
import fr.esgi.bibliotheque.penalty.application.models.CreatePenaltyRequest;
import fr.esgi.bibliotheque.penalty.application.usecases.CreatePenalty;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreatePenaltyHandler implements CreatePenalty {

    private final PenaltyRepository penaltyRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public CreatePenaltyHandler(PenaltyRepository penaltyRepository,
                                 DomainIdGenerator idGenerator,
                                 TimeProvider timeProvider) {
        this.penaltyRepository = penaltyRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public PenaltyId handle(CreatePenaltyRequest request) {
        var penalty = fr.esgi.bibliotheque.penalty.domain.Penalty.create(
                new UserId(request.userId()), request.reason(), request.amount(), idGenerator, timeProvider);
        return penaltyRepository.save(penalty).getId();
    }
}
