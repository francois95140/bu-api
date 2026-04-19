package fr.esgi.bibliotheque.circulation.application.gateways;

import fr.esgi.bibliotheque.circulation.domain.LoanPolicy;
import fr.esgi.bibliotheque.users.domain.UserCategory;

public interface CirculationPolicyGateway {
    LoanPolicy getPolicyFor(UserCategory category);
}
