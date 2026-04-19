package fr.esgi.bibliotheque.policy.application.services;

import fr.esgi.bibliotheque.policy.application.gateways.PolicyRepository;
import fr.esgi.bibliotheque.policy.application.usecases.GetPolicyForCategory;
import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetPolicyForCategoryHandler implements GetPolicyForCategory {

    private final PolicyRepository policyRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public GetPolicyForCategoryHandler(PolicyRepository policyRepository,
                                        DomainIdGenerator idGenerator,
                                        TimeProvider timeProvider) {
        this.policyRepository = policyRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public Policy handle(UserCategory category) {
        return policyRepository.findByUserCategory(category)
                .orElseGet(() -> Policy.createDefault(category, idGenerator, timeProvider));
    }
}
