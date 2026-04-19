package fr.esgi.bibliotheque.circulation.application.usecases;

import fr.esgi.bibliotheque.circulation.application.models.RenewLoanRequest;

public interface RenewLoan {
    void handle(RenewLoanRequest request);
}
