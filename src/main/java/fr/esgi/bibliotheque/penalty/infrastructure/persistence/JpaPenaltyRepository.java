package fr.esgi.bibliotheque.penalty.infrastructure.persistence;

import fr.esgi.bibliotheque.penalty.application.gateways.PenaltyRepository;
import fr.esgi.bibliotheque.penalty.domain.Penalty;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaPenaltyRepository implements PenaltyRepository {

    private final SpringJpaPenaltyRepository jpa;

    public JpaPenaltyRepository(SpringJpaPenaltyRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Penalty save(Penalty penalty) { return jpa.save(penalty); }

    @Override
    public Optional<Penalty> findById(PenaltyId id) { return jpa.findByIdValue(id.value()); }

    @Override
    public List<Penalty> findByUserId(UserId userId) { return jpa.findByUserIdValue(userId.value()); }

    @Override
    public boolean hasActivePenalty(UserId userId) { return jpa.hasActivePenaltyByUserIdValue(userId.value()); }
}
