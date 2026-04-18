package fr.esgi.bibliotheque.users.application.services;

import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.usecases.SearchUserById;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SearchUserByIdHandler implements SearchUserById {

    private final UserRepository userRepository;

    public SearchUserByIdHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(UserId id) {
        return userRepository.findById(id);
    }
}
