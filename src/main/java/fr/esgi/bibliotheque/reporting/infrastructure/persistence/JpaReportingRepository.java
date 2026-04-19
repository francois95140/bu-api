package fr.esgi.bibliotheque.reporting.infrastructure.persistence;

import fr.esgi.bibliotheque.circulation.domain.LoanStatus;
import fr.esgi.bibliotheque.reporting.application.gateways.ReportingRepository;
import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;
import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;
import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JpaReportingRepository implements ReportingRepository {

    private final EntityManager em;

    public JpaReportingRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<TopLoanEntry> findTopLoans(int limit) {
        return em.createQuery("""
                SELECT new fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry(
                    w.id.value, w.title, w.isbn, COUNT(l))
                FROM Loan l
                JOIN Copy c ON c.id.value = l.copyId.value
                JOIN Work w ON w.technicalId = c.work.technicalId
                GROUP BY w.id.value, w.title, w.isbn
                ORDER BY COUNT(l) DESC
                """, TopLoanEntry.class)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<OverdueLoanEntry> findOverdueLoans() {
        record RawOverdue(String loanId, String copyId, String userId,
                          String email, UserCategory category, Instant dueAt) {}

        var now = Instant.now();
        var rows = em.createQuery("""
                SELECT l.id.value, l.copyId.value, l.userId.value,
                       u.email, u.category, l.dueAt
                FROM Loan l
                JOIN User u ON u.id.value = l.userId.value
                WHERE l.status IN (:statuses)
                  AND l.dueAt < :now
                ORDER BY l.dueAt ASC
                """, Object[].class)
                .setParameter("statuses", List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE))
                .setParameter("now", now)
                .getResultList();

        return rows.stream()
                .map(r -> new OverdueLoanEntry(
                        (String) r[0], (String) r[1], (String) r[2],
                        (String) r[3], (UserCategory) r[4], (Instant) r[5],
                        ChronoUnit.DAYS.between((Instant) r[5], now)))
                .toList();
    }

    @Override
    public List<AcquisitionEntry> findAcquisitions(Instant from, Instant to) {
        // Justification : JPQL ne supporte pas nativement les GROUP BY par mois — on charge les données et on agrège en mémoire
        var works = em.createQuery("""
                SELECT w.createdAt FROM Work w
                WHERE w.createdAt BETWEEN :from AND :to
                """, Instant.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        var copies = em.createQuery("""
                SELECT c.createdAt FROM Copy c
                WHERE c.createdAt BETWEEN :from AND :to
                """, Instant.class)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();

        var worksByMonth = works.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        i -> YearMonth.from(i.atZone(ZoneOffset.UTC)),
                        java.util.stream.Collectors.counting()));

        var copiesByMonth = copies.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        i -> YearMonth.from(i.atZone(ZoneOffset.UTC)),
                        java.util.stream.Collectors.counting()));

        var months = new java.util.TreeSet<YearMonth>();
        months.addAll(worksByMonth.keySet());
        months.addAll(copiesByMonth.keySet());

        return months.stream()
                .map(m -> new AcquisitionEntry(m,
                        worksByMonth.getOrDefault(m, 0L),
                        copiesByMonth.getOrDefault(m, 0L)))
                .toList();
    }
}
