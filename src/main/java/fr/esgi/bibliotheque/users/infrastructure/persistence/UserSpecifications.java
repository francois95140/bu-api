package fr.esgi.bibliotheque.users.infrastructure.persistence;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import fr.esgi.bibliotheque.users.domain.UserStatus;
import org.springframework.data.jpa.domain.Specification;

class UserSpecifications {

    static Specification<User> hasName(String name) {
        return (root, query, cb) -> {
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    static Specification<User> hasEmail(String email) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    static Specification<User> hasCategory(UserCategory category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
