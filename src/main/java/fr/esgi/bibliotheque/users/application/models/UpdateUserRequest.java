package fr.esgi.bibliotheque.users.application.models;

import fr.esgi.bibliotheque.users.domain.UserCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
    @NotBlank(message = "Le prénom est obligatoire") String firstName,
    @NotBlank(message = "Le nom est obligatoire") String lastName,
    @NotNull(message = "La catégorie est obligatoire") UserCategory category
) {}
