package fr.esgi.bibliotheque.circulation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.circulation.application.gateways.CirculationCopyGateway;
import fr.esgi.bibliotheque.circulation.application.gateways.LoanRepository;
import fr.esgi.bibliotheque.circulation.application.usecases.DeclareLost;
import fr.esgi.bibliotheque.circulation.domain.LoanId;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeclareLostHandler implements DeclareLost {

    private final LoanRepository loanRepository;
    private final CirculationCopyGateway copyGateway;

    public DeclareLostHandler(LoanRepository loanRepository, CirculationCopyGateway copyGateway) {
        this.loanRepository = loanRepository;
        this.copyGateway = copyGateway;
    }

    @Override
    public void handle(LoanId loanId) {
        var loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Prêt introuvable : " + loanId.value()));

        loan.declareLost();
        loanRepository.save(loan);

        copyGateway.findByIdForUpdate(loan.getCopyId()).ifPresent(copy -> {
            copy.updateStatus(CopyStatus.LOST);
            copyGateway.save(copy);
        });
    }
}
