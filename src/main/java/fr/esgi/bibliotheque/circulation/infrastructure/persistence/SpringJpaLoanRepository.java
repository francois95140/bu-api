package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.circulation.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringJpaLoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByIdValue(String idValue);

    @Query("SELECT l FROM Loan l WHERE l.copyId.value = :copyId AND l.status IN ('ACTIVE', 'OVERDUE')")
    Optional<Loan> findActiveByCopyIdValue(@Param("copyId") String copyId);

    Optional<Loan> findByBorrowIdempotencyKey(String key);

    @Query("SELECT l FROM Loan l WHERE l.userId.value = :userId ORDER BY l.startAt DESC")
    List<Loan> findByUserIdValue(@Param("userId") String userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId.value = :userId AND l.status IN ('ACTIVE', 'OVERDUE')")
    long countActiveByUserIdValue(@Param("userId") String userId);
}
