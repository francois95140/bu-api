package fr.esgi.bibliotheque.penalty.application.usecases;

import fr.esgi.bibliotheque.penalty.domain.PenaltyId;

public interface ClearPenalty {
    void handle(PenaltyId id);
}
