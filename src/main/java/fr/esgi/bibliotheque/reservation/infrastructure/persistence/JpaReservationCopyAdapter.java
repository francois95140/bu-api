package fr.esgi.bibliotheque.reservation.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.infrastructure.persistence.SpringJpaCopyRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationCopyGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaReservationCopyAdapter implements ReservationCopyGateway {

    private final SpringJpaCopyRepository jpa;

    public JpaReservationCopyAdapter(SpringJpaCopyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Copy> findByIdForUpdate(CopyId id) {
        return jpa.findByIdValueForUpdate(id.value());
    }

    @Override
    public Copy save(Copy copy) {
        return jpa.save(copy);
    }
}
