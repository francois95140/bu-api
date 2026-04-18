package fr.esgi.bibliotheque.catalog.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import java.util.Optional;

public interface CopyRepository {
    Copy save(Copy copy);
    Optional<Copy> findById(CopyId id);
    boolean existsByBarcode(String barcode);
}
