package fr.esgi.bibliotheque.reservation.application.usecases;

import fr.esgi.bibliotheque.reservation.application.models.RequestHoldRequest;
import fr.esgi.bibliotheque.reservation.domain.Hold;

public interface RequestHold {
    Hold handle(RequestHoldRequest request);
}
