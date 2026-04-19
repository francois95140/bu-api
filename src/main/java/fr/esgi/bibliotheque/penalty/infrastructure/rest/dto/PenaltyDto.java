package fr.esgi.bibliotheque.penalty.infrastructure.rest.dto;

import fr.esgi.bibliotheque.penalty.domain.Penalty;
import fr.esgi.bibliotheque.penalty.domain.PenaltyStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PenaltyDto(
    String id,
    String userId,
    String reason,
    BigDecimal amount,
    PenaltyStatus status,
    Instant createdAt,
    Instant clearedAt
) {
    public static PenaltyDto from(Penalty penalty) {
        return new PenaltyDto(
            penalty.getId().value(),
            penalty.getUserId().value(),
            penalty.getReason(),
            penalty.getAmount(),
            penalty.getStatus(),
            penalty.getCreatedAt(),
            penalty.getClearedAt()
        );
    }
}
