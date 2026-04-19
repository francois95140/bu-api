package fr.esgi.bibliotheque.reservation.application.usecases;

import fr.esgi.bibliotheque.reservation.domain.HoldId;

public interface CancelHold {
    void handle(HoldId id);
}
