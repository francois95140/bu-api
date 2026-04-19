package fr.esgi.bibliotheque.policy.application.services;

import fr.esgi.bibliotheque.policy.application.gateways.PolicyRepository;
import fr.esgi.bibliotheque.policy.application.models.UpdatePolicyRequest;
import fr.esgi.bibliotheque.policy.application.usecases.UpdatePolicy;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdatePolicyHandler implements UpdatePolicy {

    private final PolicyRepository policyRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public UpdatePolicyHandler(PolicyRepository policyRepository,
                                DomainIdGenerator idGenerator,
                                TimeProvider timeProvider) {
        this.policyRepository = policyRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle(UserCategory category, UpdatePolicyRequest request) {
        // Justification : upsert — si la policy n'existe pas encore, on la crée avec les valeurs fournies
        var policy = policyRepository.findByUserCategory(category)
                .orElseGet(() -> fr.esgi.bibliotheque.policy.domain.Policy.createDefault(category, idGenerator, timeProvider));

        policy.update(request.maxLoans(), request.loanDurationDays(), request.maxRenewals(),
                request.overdueBlockThresholdDays(), request.penaltyType(),
                request.penaltyAmount(), request.pickupDelayDays(), timeProvider);

        policyRepository.save(policy);
    }
}
