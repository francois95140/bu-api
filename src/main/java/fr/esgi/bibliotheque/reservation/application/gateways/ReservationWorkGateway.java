package fr.esgi.bibliotheque.reservation.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.WorkId;

import java.util.Optional;

public interface ReservationWorkGateway {
    Optional<Copy> findFirstAvailableCopyForWork(WorkId workId);
}
