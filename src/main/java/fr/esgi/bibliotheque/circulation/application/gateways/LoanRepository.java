package fr.esgi.bibliotheque.circulation.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.circulation.domain.LoanId;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.List;
import java.util.Optional;

public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findById(LoanId id);
    Optional<Loan> findActiveByCopyId(CopyId copyId);
    Optional<Loan> findByBorrowIdempotencyKey(String key);
    List<Loan> findByUserId(UserId userId);
    long countActiveByUserId(UserId userId);
}
