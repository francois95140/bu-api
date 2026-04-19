package fr.esgi.bibliotheque.policy.application.gateways;

import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.users.domain.UserCategory;

import java.util.Optional;

public interface PolicyRepository {
    Policy save(Policy policy);
    Optional<Policy> findByUserCategory(UserCategory category);
}
