package fr.esgi.bibliotheque.reporting.application.models;

import fr.esgi.bibliotheque.users.domain.UserCategory;

import java.time.Instant;

public record OverdueLoanEntry(
    String loanId,
    String copyId,
    String userId,
    String userEmail,
    UserCategory userCategory,
    Instant dueAt,
    long daysOverdue
) {}
