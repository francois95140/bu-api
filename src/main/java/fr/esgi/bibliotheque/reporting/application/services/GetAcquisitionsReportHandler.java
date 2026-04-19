package fr.esgi.bibliotheque.reporting.application.services;

import fr.esgi.bibliotheque.reporting.application.gateways.ReportingRepository;
import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;
import fr.esgi.bibliotheque.reporting.application.usecases.GetAcquisitionsReport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetAcquisitionsReportHandler implements GetAcquisitionsReport {

    private final ReportingRepository reportingRepository;

    public GetAcquisitionsReportHandler(ReportingRepository reportingRepository) {
        this.reportingRepository = reportingRepository;
    }

    @Override
    public List<AcquisitionEntry> handle(Instant from, Instant to) {
        return reportingRepository.findAcquisitions(from, to);
    }
}
