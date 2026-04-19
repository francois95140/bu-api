package fr.esgi.bibliotheque.circulation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record LoanId(@Column(name = "id", updatable = false) String value) {}
