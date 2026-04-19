package fr.esgi.bibliotheque.policy.infrastructure.persistence;

import fr.esgi.bibliotheque.policy.application.gateways.PolicyRepository;
import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaPolicyRepository implements PolicyRepository {

    private final SpringJpaPolicyRepository jpa;

    public JpaPolicyRepository(SpringJpaPolicyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Policy save(Policy policy) { return jpa.save(policy); }

    @Override
    public Optional<Policy> findByUserCategory(UserCategory category) {
        return jpa.findByUserCategory(category);
    }
}
