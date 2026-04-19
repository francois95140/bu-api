package fr.esgi.bibliotheque.penalty.application.services;

import fr.esgi.bibliotheque.penalty.application.gateways.PenaltyRepository;
import fr.esgi.bibliotheque.penalty.application.usecases.SearchPenaltiesByUser;
import fr.esgi.bibliotheque.penalty.domain.Penalty;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchPenaltiesByUserHandler implements SearchPenaltiesByUser {

    private final PenaltyRepository penaltyRepository;

    public SearchPenaltiesByUserHandler(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public List<Penalty> handle(UserId userId) {
        return penaltyRepository.findByUserId(userId);
    }
}
