package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCopyStatusRequest(
    @NotNull(message = "Le statut est obligatoire") CopyStatus status
) {}
