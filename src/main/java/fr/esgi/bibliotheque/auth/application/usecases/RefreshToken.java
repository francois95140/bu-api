package fr.esgi.bibliotheque.auth.application.usecases;

import fr.esgi.bibliotheque.auth.application.models.RefreshRequest;
import fr.esgi.bibliotheque.auth.domain.TokenPair;

public interface RefreshToken {
    TokenPair handle(RefreshRequest request);
}
