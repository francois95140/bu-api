package fr.esgi.bibliotheque.reporting.application.services;

import fr.esgi.bibliotheque.reporting.application.gateways.ReportingRepository;
import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;
import fr.esgi.bibliotheque.reporting.application.usecases.GetTopLoans;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetTopLoansHandler implements GetTopLoans {

    private final ReportingRepository reportingRepository;

    public GetTopLoansHandler(ReportingRepository reportingRepository) {
        this.reportingRepository = reportingRepository;
    }

    @Override
    public List<TopLoanEntry> handle(int limit) {
        return reportingRepository.findTopLoans(limit);
    }
}
