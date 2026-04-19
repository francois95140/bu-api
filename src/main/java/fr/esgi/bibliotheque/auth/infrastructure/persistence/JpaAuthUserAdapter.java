package fr.esgi.bibliotheque.auth.infrastructure.persistence;

import fr.esgi.bibliotheque.auth.application.gateways.AuthUserGateway;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.infrastructure.persistence.SpringJpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JpaAuthUserAdapter implements AuthUserGateway {

    private final SpringJpaUserRepository jpa;

    public JpaAuthUserAdapter(SpringJpaUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public Optional<User> findById(String userId) {
        return jpa.findByIdValue(userId);
    }
}
