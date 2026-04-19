package fr.esgi.bibliotheque.circulation.application.usecases;

import fr.esgi.bibliotheque.circulation.application.models.BorrowCopyRequest;
import fr.esgi.bibliotheque.circulation.domain.Loan;

public interface BorrowCopy {
    Loan handle(BorrowCopyRequest request, String idempotencyKey);
}
