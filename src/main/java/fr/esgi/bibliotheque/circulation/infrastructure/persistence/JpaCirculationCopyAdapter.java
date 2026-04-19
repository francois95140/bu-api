package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.infrastructure.persistence.SpringJpaCopyRepository;
import fr.esgi.bibliotheque.circulation.application.gateways.CirculationCopyGateway;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaCirculationCopyAdapter implements CirculationCopyGateway {

    private final SpringJpaCopyRepository jpa;

    public JpaCirculationCopyAdapter(SpringJpaCopyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    // Justification : PESSIMISTIC_WRITE évite la race condition où deux threads empruntent le même exemplaire simultanément
    public Optional<Copy> findByIdForUpdate(CopyId id) {
        return jpa.findByIdValueForUpdate(id.value());
    }

    @Override
    public Copy save(Copy copy) {
        return jpa.save(copy);
    }
}
