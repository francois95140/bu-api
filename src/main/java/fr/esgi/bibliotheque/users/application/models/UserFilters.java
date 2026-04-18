package fr.esgi.bibliotheque.users.application.models;

import fr.esgi.bibliotheque.users.domain.UserCategory;
import fr.esgi.bibliotheque.users.domain.UserStatus;

public record UserFilters(
    String name,
    String email,
    UserCategory category,
    UserStatus status
) {}
