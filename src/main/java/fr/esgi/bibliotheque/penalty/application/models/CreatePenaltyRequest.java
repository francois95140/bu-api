package fr.esgi.bibliotheque.penalty.application.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePenaltyRequest(
    @NotBlank(message = "L'identifiant de l'utilisateur est obligatoire") String userId,
    @NotBlank(message = "La raison est obligatoire") String reason,
    @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif") BigDecimal amount
) {}
