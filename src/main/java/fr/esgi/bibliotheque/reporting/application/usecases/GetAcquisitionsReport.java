package fr.esgi.bibliotheque.reporting.application.usecases;

import fr.esgi.bibliotheque.reporting.application.models.AcquisitionEntry;

import java.time.Instant;
import java.util.List;

public interface GetAcquisitionsReport {
    List<AcquisitionEntry> handle(Instant from, Instant to);
}
