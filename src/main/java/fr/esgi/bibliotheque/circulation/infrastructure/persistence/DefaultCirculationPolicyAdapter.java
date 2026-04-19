package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.circulation.application.gateways.CirculationPolicyGateway;
import fr.esgi.bibliotheque.circulation.domain.LoanPolicy;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.stereotype.Component;

// Justification : implémentation provisoire avec valeurs par défaut — sera remplacée par le module policy/
@Component
public class DefaultCirculationPolicyAdapter implements CirculationPolicyGateway {

    @Override
    public LoanPolicy getPolicyFor(UserCategory category) {
        return switch (category) {
            case TEACHER -> new LoanPolicy(20, 60, 3);
            case LIBRARIAN, ADMIN -> new LoanPolicy(20, 60, 3);
            default -> new LoanPolicy(5, 21, 2); // STUDENT
        };
    }
}
