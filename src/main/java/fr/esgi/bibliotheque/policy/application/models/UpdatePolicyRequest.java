package fr.esgi.bibliotheque.policy.application.models;

import fr.esgi.bibliotheque.policy.domain.PenaltyType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record UpdatePolicyRequest(
    @Positive(message = "Le nombre max d'emprunts doit être positif") int maxLoans,
    @Positive(message = "La durée de prêt doit être positive") int loanDurationDays,
    @PositiveOrZero(message = "Le nombre max de prolongations doit être positif ou zéro") int maxRenewals,
    @Positive(message = "Le seuil de blocage doit être positif") int overdueBlockThresholdDays,
    @NotNull(message = "Le type de pénalité est obligatoire") PenaltyType penaltyType,
    @NotNull @DecimalMin(value = "0.0", message = "Le montant de pénalité ne peut pas être négatif") BigDecimal penaltyAmount,
    @Positive(message = "Le délai de retrait doit être positif") int pickupDelayDays
) {}
