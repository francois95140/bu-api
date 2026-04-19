package fr.esgi.bibliotheque.reservation.infrastructure.rest.dto;

import fr.esgi.bibliotheque.reservation.domain.Hold;
import fr.esgi.bibliotheque.reservation.domain.HoldStatus;

import java.time.Instant;

public record HoldDto(
    String id,
    String workId,
    String userId,
    String copyId,
    HoldStatus status,
    int queuePosition,
    Instant pickupUntil,
    Instant createdAt
) {
    public static HoldDto from(Hold hold) {
        return new HoldDto(
            hold.getId().value(),
            hold.getWorkId().value(),
            hold.getUserId().value(),
            hold.getCopyId() != null ? hold.getCopyId().value() : null,
            hold.getStatus(),
            hold.getQueuePosition(),
            hold.getPickupUntil(),
            hold.getCreatedAt()
        );
    }
}
