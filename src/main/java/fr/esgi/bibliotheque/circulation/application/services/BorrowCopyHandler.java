package fr.esgi.bibliotheque.circulation.application.services;

import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;
import fr.esgi.bibliotheque.circulation.application.gateways.*;
import fr.esgi.bibliotheque.circulation.application.models.BorrowCopyRequest;
import fr.esgi.bibliotheque.circulation.application.usecases.BorrowCopy;
import fr.esgi.bibliotheque.circulation.domain.Loan;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import fr.esgi.bibliotheque.users.domain.UserId;
import fr.esgi.bibliotheque.users.domain.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BorrowCopyHandler implements BorrowCopy {

    private final LoanRepository loanRepository;
    private final CirculationCopyGateway copyGateway;
    private final CirculationUserGateway userGateway;
    private final CirculationPolicyGateway policyGateway;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public BorrowCopyHandler(LoanRepository loanRepository,
                              CirculationCopyGateway copyGateway,
                              CirculationUserGateway userGateway,
                              CirculationPolicyGateway policyGateway,
                              DomainIdGenerator idGenerator,
                              TimeProvider timeProvider) {
        this.loanRepository = loanRepository;
        this.copyGateway = copyGateway;
        this.userGateway = userGateway;
        this.policyGateway = policyGateway;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public Loan handle(BorrowCopyRequest request, String idempotencyKey) {
        // Idempotence : si la clé a déjà été traitée, on retourne le prêt existant
        if (idempotencyKey != null) {
            var existing = loanRepository.findByBorrowIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        var userId = new UserId(request.userId());
        var copyId = new CopyId(request.copyId());

        var user = userGateway.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + request.userId()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Le compte de l'utilisateur est bloqué ou suspendu");
        }

        var policy = policyGateway.getPolicyFor(user.getCategory());

        long activeLoans = loanRepository.countActiveByUserId(userId);
        if (activeLoans >= policy.maxLoans()) {
            throw new BusinessException("Quota d'emprunts atteint (" + policy.maxLoans() + " max)");
        }

        // Justification : lock pessimiste pour éviter deux emprunts simultanés du même exemplaire
        var copy = copyGateway.findByIdForUpdate(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Exemplaire introuvable : " + request.copyId()));

        if (copy.getStatus() != CopyStatus.AVAILABLE) {
            throw new BusinessException("L'exemplaire n'est pas disponible (statut : " + copy.getStatus() + ")");
        }

        copy.updateStatus(CopyStatus.ON_LOAN);
        copyGateway.save(copy);

        var loan = Loan.create(copyId, userId, policy.loanDurationDays(), idempotencyKey, idGenerator, timeProvider);
        return loanRepository.save(loan);
    }
}
