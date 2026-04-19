package fr.esgi.bibliotheque.reporting.application.gateways;

import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;
import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;
import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;

import java.time.Instant;
import java.util.List;

public interface ReportingRepository {
    List<TopLoanEntry> findTopLoans(int limit);
    List<OverdueLoanEntry> findOverdueLoans();
    List<AcquisitionEntry> findAcquisitions(Instant from, Instant to);
}
