package fr.esgi.bibliotheque.catalog.application.models;

public record WorkFilters(String title, String isbn, String authors, String subject) {
    public static WorkFilters empty() {
        return new WorkFilters(null, null, null, null);
    }
}
