package fr.esgi.bibliotheque.circulation.application.usecases;

import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.List;

public interface SearchLoansByUser {
    List<Loan> handle(UserId userId);
}
