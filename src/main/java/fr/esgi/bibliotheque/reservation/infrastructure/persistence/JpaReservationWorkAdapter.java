package fr.esgi.bibliotheque.reservation.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.catalog.infrastructure.persistence.SpringJpaCopyRepository;
import fr.esgi.bibliotheque.reservation.application.gateways.ReservationWorkGateway;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaReservationWorkAdapter implements ReservationWorkGateway {

    private final SpringJpaCopyRepository jpa;

    public JpaReservationWorkAdapter(SpringJpaCopyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Copy> findFirstAvailableCopyForWork(WorkId workId) {
        return jpa.findFirstAvailableCopyByWorkId(workId.value(), CopyStatus.AVAILABLE);
    }
}
