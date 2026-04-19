package fr.esgi.bibliotheque.reporting.application.usecases;

import fr.esgi.bibliotheque.reporting.application.models.TopLoanEntry;

import java.util.List;

public interface GetTopLoans {
    List<TopLoanEntry> handle(int limit);
}
