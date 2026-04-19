package fr.esgi.bibliotheque.penalty.application.usecases;

import fr.esgi.bibliotheque.penalty.application.models.CreatePenaltyRequest;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;

public interface CreatePenalty {
    PenaltyId handle(CreatePenaltyRequest request);
}
