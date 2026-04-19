package fr.esgi.bibliotheque.reservation.infrastructure.persistence;

import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.reservation.application.gateways.HoldRepository;
import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.reservation.domain.HoldId;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class JpaHoldRepository implements HoldRepository {

    private final SpringJpaHoldRepository jpa;

    public JpaHoldRepository(SpringJpaHoldRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Hold save(Hold hold) { return jpa.save(hold); }

    @Override
    public Optional<Hold> findById(HoldId id) { return jpa.findByIdValue(id.value()); }

    @Override
    public List<Hold> findActiveByWorkId(WorkId workId) {
        return jpa.findActiveByWorkIdValue(workId.value());
    }

    @Override
    public Optional<Hold> findActiveByWorkIdAndUserId(WorkId workId, UserId userId) {
        return jpa.findActiveByWorkIdValueAndUserIdValue(workId.value(), userId.value());
    }

    @Override
    public List<Hold> findExpiredReadyForPickup(Instant before) {
        return jpa.findExpiredReadyForPickup(before);
    }

    @Override
    public int countActiveByWorkId(WorkId workId) {
        return jpa.countActiveByWorkIdValue(workId.value());
    }
}
