package fr.esgi.bibliotheque.reservation.infrastructure.persistence;

import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.reservation.domain.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface SpringJpaHoldRepository extends JpaRepository<Hold, Long> {

    Optional<Hold> findByIdValue(String value);

    @Query("SELECT h FROM Hold h WHERE h.workId.value = :workId AND h.status IN ('REQUESTED','QUEUED','READY_FOR_PICKUP') ORDER BY h.createdAt ASC")
    List<Hold> findActiveByWorkIdValue(@Param("workId") String workId);

    @Query("SELECT h FROM Hold h WHERE h.workId.value = :workId AND h.userId.value = :userId AND h.status IN ('REQUESTED','QUEUED','READY_FOR_PICKUP')")
    Optional<Hold> findActiveByWorkIdValueAndUserIdValue(@Param("workId") String workId, @Param("userId") String userId);

    @Query("SELECT h FROM Hold h WHERE h.status = 'READY_FOR_PICKUP' AND h.pickupUntil < :now")
    List<Hold> findExpiredReadyForPickup(@Param("now") Instant now);

    @Query("SELECT COUNT(h) FROM Hold h WHERE h.workId.value = :workId AND h.status IN ('REQUESTED','QUEUED','READY_FOR_PICKUP')")
    int countActiveByWorkIdValue(@Param("workId") String workId);
}
