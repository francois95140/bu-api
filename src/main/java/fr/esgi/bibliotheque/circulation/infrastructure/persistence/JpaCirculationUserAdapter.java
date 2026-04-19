package fr.esgi.bibliotheque.circulation.infrastructure.persistence;

import fr.esgi.bibliotheque.circulation.application.gateways.CirculationUserGateway;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;
import fr.esgi.bibliotheque.users.infrastructure.persistence.SpringJpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaCirculationUserAdapter implements CirculationUserGateway {

    private final SpringJpaUserRepository jpa;

    public JpaCirculationUserAdapter(SpringJpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findByIdValue(id.value());
    }
}
