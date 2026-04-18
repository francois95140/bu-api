package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaWorkRepository implements WorkRepository {

    private final SpringJpaWorkRepository spring;
    private final WorkSpecificationBuilder specBuilder;

    public JpaWorkRepository(SpringJpaWorkRepository spring) {
        this.spring = spring;
        this.specBuilder = new WorkSpecificationBuilder();
    }

    @Override
    public Work save(Work work) {
        return spring.save(work);
    }

    @Override
    public Optional<Work> findById(WorkId id) {
        return spring.findByIdValue(id.value());
    }

    @Override
    public Optional<Work> findByIdWithCopies(WorkId id) {
        return spring.findByIdValueWithCopies(id.value());
    }

    @Override
    public List<Work> findAll(WorkFilters filters) {
        var spec = specBuilder.build(filters);
        return spring.findAll(spec, PageRequest.of(0, 1000, Sort.by("title").ascending())).getContent();
    }

    @Override
    public void deleteById(WorkId id) {
        spring.findByIdValue(id.value()).ifPresent(spring::delete);
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return spring.existsByIsbn(isbn);
    }
}
