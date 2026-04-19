package fr.esgi.bibliotheque.circulation.application.models;

import jakarta.validation.constraints.NotBlank;

public record BorrowCopyRequest(
    @NotBlank(message = "L'identifiant de l'exemplaire est obligatoire") String copyId,
    @NotBlank(message = "L'identifiant de l'utilisateur est obligatoire") String userId
) {}
