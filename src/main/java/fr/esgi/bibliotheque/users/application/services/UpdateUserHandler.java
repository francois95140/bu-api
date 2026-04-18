package fr.esgi.bibliotheque.users.application.services;

import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.models.UpdateUserRequest;
import fr.esgi.bibliotheque.users.application.usecases.UpdateUser;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateUserHandler implements UpdateUser {

    private final UserRepository userRepository;

    public UpdateUserHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void handle(UserId id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id.value()));
        user.update(request.firstName(), request.lastName(), request.category());
        userRepository.save(user);
    }
}
