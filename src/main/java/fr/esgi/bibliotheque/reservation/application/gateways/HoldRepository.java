package fr.esgi.bibliotheque.reservation.application.gateways;

import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.reservation.domain.HoldId;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HoldRepository {
    Hold save(Hold hold);
    Optional<Hold> findById(HoldId id);
    List<Hold> findActiveByWorkId(WorkId workId);
    Optional<Hold> findActiveByWorkIdAndUserId(WorkId workId, UserId userId);
    List<Hold> findExpiredReadyForPickup(Instant before);
    int countActiveByWorkId(WorkId workId);
}
