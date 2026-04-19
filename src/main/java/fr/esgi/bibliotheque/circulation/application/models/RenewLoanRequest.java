package fr.esgi.bibliotheque.circulation.application.models;

import jakarta.validation.constraints.NotBlank;

public record RenewLoanRequest(
    @NotBlank(message = "L'identifiant du prêt est obligatoire") String loanId
) {}
