package fr.esgi.bibliotheque.circulation.application.services;

import fr.esgi.bibliotheque.circulation.application.gateways.CirculationPolicyGateway;
import fr.esgi.bibliotheque.circulation.application.gateways.CirculationUserGateway;
import fr.esgi.bibliotheque.circulation.application.gateways.LoanRepository;
import fr.esgi.bibliotheque.circulation.application.models.RenewLoanRequest;
import fr.esgi.bibliotheque.circulation.application.usecases.RenewLoan;
import fr.esgi.bibliotheque.circulation.domain.LoanId;
import fr.esgi.bibliotheque.circulation.domain.LoanStatus;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RenewLoanHandler implements RenewLoan {

    private final LoanRepository loanRepository;
    private final CirculationUserGateway userGateway;
    private final CirculationPolicyGateway policyGateway;
    private final TimeProvider timeProvider;

    public RenewLoanHandler(LoanRepository loanRepository,
                             CirculationUserGateway userGateway,
                             CirculationPolicyGateway policyGateway,
                             TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.userGateway = userGateway;
        this.policyGateway = policyGateway;
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle(RenewLoanRequest request) {
        var loan = loanRepository.findById(new LoanId(request.loanId()))
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable : " + request.loanId()));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Seul un prêt actif peut être prolongé");
        }

        var user = userGateway.findById(loan.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        var policy = policyGateway.getPolicyFor(user.getCategory());

        if (loan.getRenewCount() >= policy.maxRenewals()) {
            throw new BusinessException("Nombre maximal de prolongations atteint (" + policy.maxRenewals() + ")");
        }

        // TODO : vérifier l'absence de Hold en attente sur le Work (à implémenter avec le module reservation/)

        loan.renew(policy.loanDurationDays(), timeProvider);
        loanRepository.save(loan);
    }
}
