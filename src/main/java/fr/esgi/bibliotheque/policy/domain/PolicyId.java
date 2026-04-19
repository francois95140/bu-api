package fr.esgi.bibliotheque.policy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PolicyId(@Column(name = "id", updatable = false) String value) {}
