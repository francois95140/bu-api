package fr.esgi.bibliotheque.circulation.infrastructure.rest.dto;

import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.circulation.domain.LoanStatus;

import java.time.Instant;

public record LoanDto(
    String id,
    String copyId,
    String userId,
    Instant startAt,
    Instant dueAt,
    Instant returnedAt,
    int renewCount,
    LoanStatus status
) {
    public static LoanDto from(Loan loan) {
        return new LoanDto(
            loan.getId().value(),
            loan.getCopyId().value(),
            loan.getUserId().value(),
            loan.getStartAt(),
            loan.getDueAt(),
            loan.getReturnedAt(),
            loan.getRenewCount(),
            loan.getStatus()
        );
    }
}
