package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

interface SpringJpaWorkRepository extends JpaRepository<Work, Long>, JpaSpecificationExecutor<Work> {

    Optional<Work> findByIdValue(String value);

    @Query("SELECT DISTINCT w FROM Work w LEFT JOIN FETCH w.copies WHERE w.id.value = :value")
    Optional<Work> findByIdValueWithCopies(@Param("value") String value);

    boolean existsByIsbn(String isbn);
}
