package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.circulation.application.gateways.LoanRepository;
import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.circulation.domain.LoanId;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaLoanRepository implements LoanRepository {

    private final SpringJpaLoanRepository jpa;

    public JpaLoanRepository(SpringJpaLoanRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Loan save(Loan loan) {
        return jpa.save(loan);
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return jpa.findByIdValue(id.value());
    }

    @Override
    public Optional<Loan> findActiveByCopyId(CopyId copyId) {
        return jpa.findActiveByCopyIdValue(copyId.value());
    }

    @Override
    public Optional<Loan> findByBorrowIdempotencyKey(String key) {
        return jpa.findByBorrowIdempotencyKey(key);
    }

    @Override
    public List<Loan> findByUserId(UserId userId) {
        return jpa.findByUserIdValue(userId.value());
    }

    @Override
    public long countActiveByUserId(UserId userId) {
        return jpa.countActiveByUserIdValue(userId.value());
    }
}
