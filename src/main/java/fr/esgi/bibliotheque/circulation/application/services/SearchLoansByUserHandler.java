package fr.esgi.bibliotheque.circulation.application.services;

import fr.esgi.bibliotheque.circulation.application.gateways.LoanRepository;
import fr.esgi.bibliotheque.circulation.application.usecases.SearchLoansByUser;
import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchLoansByUserHandler implements SearchLoansByUser {

    private final LoanRepository loanRepository;

    public SearchLoansByUserHandler(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public List<Loan> handle(UserId userId) {
        return loanRepository.findByUserId(userId);
    }
}
