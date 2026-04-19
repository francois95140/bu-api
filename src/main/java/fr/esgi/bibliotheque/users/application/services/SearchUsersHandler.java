package fr.esgi.bibliotheque.users.application.services;

import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.application.usecases.SearchUsers;
import fr.esgi.bibliotheque.users.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchUsersHandler implements SearchUsers {

    private final UserRepository userRepository;

    public SearchUsersHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(UserFilters filters) {
        return userRepository.search(filters);
    }
}
