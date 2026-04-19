package fr.esgi.bibliotheque.reporting.application.models;

import java.time.YearMonth;

public record AcquisitionEntry(YearMonth month, long worksAdded, long copiesAdded) {}
