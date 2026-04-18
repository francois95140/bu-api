package fr.esgi.bibliotheque.shared.error;

public record ValidationError(String field, Object rejectedValue, String message) {}
