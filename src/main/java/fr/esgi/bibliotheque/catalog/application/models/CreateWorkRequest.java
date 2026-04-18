package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.shared.validation.ValidIsbn;
import jakarta.validation.constraints.*;

public record CreateWorkRequest(
    @NotBlank(message = "L'ISBN est obligatoire") @ValidIsbn String isbn,
    @NotBlank(message = "Le titre est obligatoire") @Size(min = 1, max = 255) String title,
    @NotBlank(message = "Les auteurs sont obligatoires") @Size(max = 500) String authors,
    @Size(max = 255) String publisher,
    @Min(value = 1000, message = "L'année doit être valide") @Max(value = 2100) Integer year,
    @Size(max = 255) String subject,
    @Size(max = 10) String language,
    String description
) {}
