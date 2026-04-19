package fr.esgi.bibliotheque.policy.infrastructure.rest.dto;

import fr.esgi.bibliotheque.policy.domain.PenaltyType;
import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.users.domain.UserCategory;

import java.math.BigDecimal;

public record PolicyDto(
    String id,
    UserCategory userCategory,
    int maxLoans,
    int loanDurationDays,
    int maxRenewals,
    int overdueBlockThresholdDays,
    PenaltyType penaltyType,
    BigDecimal penaltyAmount,
    int pickupDelayDays
) {
    public static PolicyDto from(Policy policy) {
        return new PolicyDto(
            policy.getId().value(),
            policy.getUserCategory(),
            policy.getMaxLoans(),
            policy.getLoanDurationDays(),
            policy.getMaxRenewals(),
            policy.getOverdueBlockThresholdDays(),
            policy.getPenaltyType(),
            policy.getPenaltyAmount(),
            policy.getPickupDelayDays()
        );
    }
}
