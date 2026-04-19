package fr.esgi.bibliotheque.circulation.application.usecases;

import fr.esgi.bibliotheque.catalog.domain.CopyId;

public interface ReturnCopy {
    void handle(CopyId copyId, String idempotencyKey);
}
