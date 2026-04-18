package fr.esgi.bibliotheque.catalog.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaCopyRepository implements CopyRepository {

    private final SpringJpaCopyRepository spring;

    public JpaCopyRepository(SpringJpaCopyRepository spring) {
        this.spring = spring;
    }

    @Override
    public Copy save(Copy copy) {
        return spring.save(copy);
    }

    @Override
    public Optional<Copy> findById(CopyId id) {
        return spring.findByIdValue(id.value());
    }

    @Override
    public boolean existsByBarcode(String barcode) {
        return spring.existsByBarcode(barcode);
    }
}
