package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.circulation.application.gateways.CirculationPolicyGateway;
import fr.esgi.bibliotheque.circulation.domain.LoanPolicy;
import fr.esgi.bibliotheque.policy.application.usecases.GetPolicyForCategory;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.stereotype.Component;

@Component
public class DefaultCirculationPolicyAdapter implements CirculationPolicyGateway {

    private final GetPolicyForCategory getPolicyForCategory;

    public DefaultCirculationPolicyAdapter(GetPolicyForCategory getPolicyForCategory) {
        this.getPolicyForCategory = getPolicyForCategory;
    }

    @Override
    public LoanPolicy getPolicyFor(UserCategory category) {
        var policy = getPolicyForCategory.handle(category);
        return new LoanPolicy(policy.getMaxLoans(), policy.getLoanDurationDays(), policy.getMaxRenewals());
    }
}
