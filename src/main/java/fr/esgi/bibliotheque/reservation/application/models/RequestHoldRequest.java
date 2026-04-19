package fr.esgi.bibliotheque.reservation.application.models;

import jakarta.validation.constraints.NotBlank;

public record RequestHoldRequest(
    @NotBlank(message = "L'identifiant de l'ouvrage est obligatoire") String workId,
    @NotBlank(message = "L'identifiant de l'utilisateur est obligatoire") String userId
) {}
