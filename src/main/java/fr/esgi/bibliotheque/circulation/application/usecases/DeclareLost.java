package fr.esgi.bibliotheque.circulation.application.usecases;

import fr.esgi.bibliotheque.circulation.domain.LoanId;

public interface DeclareLost {
    void handle(LoanId loanId);
}
