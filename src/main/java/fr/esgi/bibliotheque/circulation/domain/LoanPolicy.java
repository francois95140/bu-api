package fr.esgi.bibliotheque.circulation.domain;

public record LoanPolicy(int maxLoans, int loanDurationDays, int maxRenewals) {}
