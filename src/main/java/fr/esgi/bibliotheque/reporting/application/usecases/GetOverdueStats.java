package fr.esgi.bibliotheque.reporting.application.usecases;

import fr.esgi.bibliotheque.reporting.application.models.OverdueLoanEntry;

import java.util.List;

public interface GetOverdueStats {
    List<OverdueLoanEntry> handle();
}
