package fr.esgi.bibliotheque.circulation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.circulation.application.gateways.CirculationCopyGateway;
import fr.esgi.bibliotheque.circulation.application.gateways.LoanRepository;
import fr.esgi.bibliotheque.circulation.application.usecases.ReturnCopy;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReturnCopyHandler implements ReturnCopy {

    private final LoanRepository loanRepository;
    private final CirculationCopyGateway copyGateway;
    private final TimeProvider timeProvider;

    public ReturnCopyHandler(LoanRepository loanRepository,
                              CirculationCopyGateway copyGateway,
                              TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.copyGateway = copyGateway;
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle(CopyId copyId, String idempotencyKey) {
        var loan = loanRepository.findActiveByCopyId(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun prêt actif pour l'exemplaire : " + copyId.value()));

        // Idempotence : si le retour a déjà été enregistré avec cette clé, on ignore
        if (idempotencyKey != null && idempotencyKey.equals(loan.getReturnIdempotencyKey())) {
            return;
        }

        loan.returnCopy(idempotencyKey, timeProvider);
        loanRepository.save(loan);

        var copy = copyGateway.findByIdForUpdate(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Exemplaire introuvable : " + copyId.value()));
        copy.updateStatus(CopyStatus.AVAILABLE);
        copyGateway.save(copy);
    }
}
