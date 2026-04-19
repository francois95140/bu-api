package fr.esgi.bibliotheque.reporting.infrastructure.persistence;

import fr.esgi.bibliotheque.reporting.application.gateways.ReportingRepository;
import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;
import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;
import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
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
        return em.createQuery("""
                SELECT new fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry(
                    l.id.value, l.copyId.value, l.userId.value,
                    u.email, u.category, l.dueAt,
                    FUNCTION('DATEDIFF', CURRENT_TIMESTAMP, l.dueAt))
                FROM Loan l
                JOIN User u ON u.id.value = l.userId.value
                WHERE l.status IN ('ACTIVE', 'OVERDUE')
                  AND l.dueAt < CURRENT_TIMESTAMP
                ORDER BY l.dueAt ASC
                """, OverdueLoanEntry.class)
                .getResultList();
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
