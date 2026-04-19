package fr.esgi.bibliotheque.penalty.infrastructure.persistence;

import fr.esgi.bibliotheque.penalty.domain.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface SpringJpaPenaltyRepository extends JpaRepository<Penalty, Long> {

    Optional<Penalty> findByIdValue(String value);

    @Query("SELECT p FROM Penalty p WHERE p.userId.value = :userId ORDER BY p.createdAt DESC")
    List<Penalty> findByUserIdValue(@Param("userId") String userId);

    @Query("SELECT COUNT(p) > 0 FROM Penalty p WHERE p.userId.value = :userId AND p.status = 'PENDING'")
    boolean hasActivePenaltyByUserIdValue(@Param("userId") String userId);
}
