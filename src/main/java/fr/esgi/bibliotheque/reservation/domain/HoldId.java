package fr.esgi.bibliotheque.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record HoldId(@Column(name = "id", updatable = false) String value) {}
