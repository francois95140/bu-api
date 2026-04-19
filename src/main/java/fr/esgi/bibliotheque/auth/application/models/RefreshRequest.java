package fr.esgi.bibliotheque.auth.application.models;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {}
