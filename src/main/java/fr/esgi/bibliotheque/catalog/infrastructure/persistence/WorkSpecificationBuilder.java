package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.data.jpa.domain.Specification;

public class WorkSpecificationBuilder {

    public Specification<Work> build(WorkFilters filters) {
        Specification<Work> spec = (root, query, cb) -> cb.conjunction();
        if (filters.title() != null && !filters.title().isBlank()) {
            spec = spec.and(WorkSpecifications.titleLike(filters.title()));
        }
        if (filters.isbn() != null && !filters.isbn().isBlank()) {
            spec = spec.and(WorkSpecifications.isbnEquals(filters.isbn()));
        }
        if (filters.authors() != null && !filters.authors().isBlank()) {
            spec = spec.and(WorkSpecifications.authorsLike(filters.authors()));
        }
        if (filters.subject() != null && !filters.subject().isBlank()) {
            spec = spec.and(WorkSpecifications.subjectEquals(filters.subject()));
        }
        return spec;
    }
}
