package fr.esgi.bibliotheque.users.application.services;

import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.users.application.gateways.UserRepository;
import fr.esgi.bibliotheque.users.application.models.RegisterUserRequest;
import fr.esgi.bibliotheque.users.application.usecases.RegisterUser;
import fr.esgi.bibliotheque.users.domain.User;
import fr.esgi.bibliotheque.users.domain.UserId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterUserHandler implements RegisterUser {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    private final UserRepository userRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public RegisterUserHandler(UserRepository userRepository,
                                DomainIdGenerator idGenerator,
                                TimeProvider timeProvider) {
        this.userRepository = userRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public UserId handle(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Un compte existe déjà avec l'email : " + request.email());
        }
        String passwordHash = encoder.encode(request.password());
        User user = User.create(request.firstName(), request.lastName(),
                request.email(), passwordHash, request.category(), idGenerator, timeProvider);
        return userRepository.save(user).getId();
    }
}
