package fr.esgi.bibliotheque.users.infrastructure.persistence;

import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SpringJpaUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByIdValue(String idValue);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
