package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record WorkId(@Column(name = "id", updatable = false) String value) {}
