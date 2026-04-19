package fr.esgi.bibliotheque.penalty.application.gateways;

import fr.esgi.bibliotheque.penalty.domain.Penalty;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface PenaltyRepository {
    Penalty save(Penalty penalty);
    Optional<Penalty> findById(PenaltyId id);
    List<Penalty> findByUserId(UserId userId);
    boolean hasActivePenalty(UserId userId);
}
