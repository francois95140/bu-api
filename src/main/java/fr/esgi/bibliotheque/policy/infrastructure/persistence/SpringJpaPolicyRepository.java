package fr.esgi.bibliotheque.policy.infrastructure.persistence;

import fr.esgi.bibliotheque.policy.domain.Policy;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringJpaPolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByUserCategory(UserCategory category);
}
