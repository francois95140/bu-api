package fr.esgi.bibliotheque.users.infrastructure.persistence;

import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.domain.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
class UserSpecificationBuilder {

    Specification<User> build(UserFilters filters) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (filters.name() != null && !filters.name().isBlank()) {
            spec = spec.and(UserSpecifications.hasName(filters.name()));
        }
        if (filters.email() != null && !filters.email().isBlank()) {
            spec = spec.and(UserSpecifications.hasEmail(filters.email()));
        }
        if (filters.category() != null) {
            spec = spec.and(UserSpecifications.hasCategory(filters.category()));
        }
        if (filters.status() != null) {
            spec = spec.and(UserSpecifications.hasStatus(filters.status()));
        }
        return spec;
    }
}
