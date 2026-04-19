package fr.esgi.bibliotheque.penalty.application.usecases;

import fr.esgi.bibliotheque.penalty.domain.Penalty;
import fr.esgi.bibliotheque.users.domain.UserId;

import java.util.List;

public interface SearchPenaltiesByUser {
    List<Penalty> handle(UserId userId);
}
