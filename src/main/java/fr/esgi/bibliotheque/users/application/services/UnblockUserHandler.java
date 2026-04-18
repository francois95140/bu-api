package fr.esgi.bibliotheque.users.application.services;

import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.usecases.UnblockUser;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UnblockUserHandler implements UnblockUser {

    private final UserRepository userRepository;

    public UnblockUserHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(UserId id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id.value()));
        user.unblock();
        userRepository.save(user);
    }
}
