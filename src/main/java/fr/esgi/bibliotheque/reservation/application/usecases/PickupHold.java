package fr.esgi.bibliotheque.reservation.application.usecases;

import fr.esgi.bibliotheque.reservation.domain.HoldId;

public interface PickupHold {
    void handle(HoldId id);
}
