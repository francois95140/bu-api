package fr.esgi.bibliotheque.policy.application.usecases;

import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.users.domain.UserCategory;

public interface GetPolicyForCategory {
    Policy handle(UserCategory category);
}
