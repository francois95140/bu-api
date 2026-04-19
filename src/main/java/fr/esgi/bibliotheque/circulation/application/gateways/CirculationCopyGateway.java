package fr.esgi.bibliotheque.circulation.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;

import java.util.Optional;

public interface CirculationCopyGateway {
    // Justification : lock pessimiste pour éviter les emprunts concurrents sur le même exemplaire
    Optional<Copy> findByIdForUpdate(CopyId id);
    Copy save(Copy copy);
}
