package fr.esgi.bibliotheque.catalog.application.models;

import jakarta.validation.constraints.*;

public record UpdateWorkRequest(
    @NotBlank(message = "Le titre est obligatoire") @Size(min = 1, max = 255) String title,
    @NotBlank(message = "Les auteurs sont obligatoires") @Size(max = 500) String authors,
    @Size(max = 255) String publisher,
    @Min(value = 1000) @Max(value = 2100) Integer year,
    @Size(max = 255) String subject,
    @Size(max = 10) String language,
    String description
) {}
