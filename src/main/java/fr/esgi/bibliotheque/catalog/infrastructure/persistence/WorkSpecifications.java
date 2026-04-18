package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.data.jpa.domain.Specification;

public final class WorkSpecifications {

    private WorkSpecifications() {}

    public static Specification<Work> titleLike(String title) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Work> isbnEquals(String isbn) {
        return (root, query, cb) ->
            cb.equal(root.get("isbn"), isbn);
    }

    public static Specification<Work> authorsLike(String authors) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("authors")), "%" + authors.toLowerCase() + "%");
    }

    public static Specification<Work> subjectEquals(String subject) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("subject")), "%" + subject.toLowerCase() + "%");
    }
}
