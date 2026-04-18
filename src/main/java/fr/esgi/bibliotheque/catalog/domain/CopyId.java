package fr.esgi.bibliotheque.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record CopyId(@Column(name = "id", updatable = false) String value) {}
