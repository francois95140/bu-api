package fr.esgi.bibliotheque.reporting.application.services;

import fr.esgi.bibliotheque.reporting.application.gateways.ReportingRepository;
import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;
import fr.esgi.bibliotheque.reporting.application.usecases.GetOverdueStats;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetOverdueStatsHandler implements GetOverdueStats {

    private final ReportingRepository reportingRepository;

    public GetOverdueStatsHandler(ReportingRepository reportingRepository) {
        this.reportingRepository = reportingRepository;
    }

    @Override
    public List<OverdueLoanEntry> handle() {
        return reportingRepository.findOverdueLoans();
    }
}
