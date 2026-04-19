package fr.esgi.bibliotheque.reservation.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;

import java.util.Optional;

public interface ReservationCopyGateway {
    Optional<Copy> findByIdForUpdate(CopyId id);
    Copy save(Copy copy);
}
