package fr.esgi.bibliotheque.penalty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PenaltyId(@Column(name = "id", updatable = false) String value) {}
