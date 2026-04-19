package fr.esgi.bibliotheque.reporting.infrastructure.rest;

import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;
import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;
import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;
import fr.esgi.bibliotheque.reporting.application.usecases.GetAcquisitionsReport;
import fr.esgi.bibliotheque.reporting.application.usecases.GetOverdueStats;
import fr.esgi.bibliotheque.reporting.application.usecases.GetTopLoans;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final GetTopLoans getTopLoans;
    private final GetOverdueStats getOverdueStats;
    private final GetAcquisitionsReport getAcquisitionsReport;

    public ReportingController(GetTopLoans getTopLoans, GetOverdueStats getOverdueStats,
                                GetAcquisitionsReport getAcquisitionsReport) {
        this.getTopLoans = getTopLoans;
        this.getOverdueStats = getOverdueStats;
        this.getAcquisitionsReport = getAcquisitionsReport;
    }

    @GetMapping("/top-loans")
    public List<TopLoanEntry> topLoans(@RequestParam(defaultValue = "10") int limit) {
        return getTopLoans.handle(limit);
    }

    @GetMapping("/overdue")
    public List<OverdueLoanEntry> overdue() {
        return getOverdueStats.handle();
    }

    @GetMapping("/acquisitions")
    public List<AcquisitionEntry> acquisitions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return getAcquisitionsReport.handle(from, to);
    }
}
