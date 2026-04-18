package fr.esgi.bibliotheque.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record UserId(@Column(name = "id", updatable = false) String value) {}
