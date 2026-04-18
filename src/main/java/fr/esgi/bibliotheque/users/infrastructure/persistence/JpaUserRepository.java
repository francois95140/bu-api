package fr.esgi.bibliotheque.users.infrastructure.persistence;

import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaUserRepository implements UserRepository {

    private final SpringJpaUserRepository jpa;
    private final UserSpecificationBuilder specBuilder;

    public JpaUserRepository(SpringJpaUserRepository jpa, UserSpecificationBuilder specBuilder) {
        this.jpa = jpa;
        this.specBuilder = specBuilder;
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jpa.findByIdValue(id.value());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public List<User> search(UserFilters filters) {
        return jpa.findAll(specBuilder.build(filters));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }
}
