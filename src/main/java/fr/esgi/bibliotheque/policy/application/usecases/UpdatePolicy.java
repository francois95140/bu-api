package fr.esgi.bibliotheque.policy.application.usecases;

import fr.esgi.bibliotheque.policy.application.models.UpdatePolicyRequest;
import fr.esgi.bibliotheque.users.domain.UserCategory;

public interface UpdatePolicy {
    void handle(UserCategory category, UpdatePolicyRequest request);
}
