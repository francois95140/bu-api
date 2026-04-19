package fr.esgi.bibliotheque.auth.infrastructure.rest;

import fr.esgi.bibliotheque.auth.application.models.AuthRequest;
import fr.esgi.bibliotheque.auth.application.models.RefreshRequest;
import fr.esgi.bibliotheque.auth.application.usecases.Authenticate;
import fr.esgi.bibliotheque.auth.application.usecases.RefreshToken;
import fr.esgi.bibliotheque.auth.domain.TokenPair;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Authenticate authenticate;
    private final RefreshToken refreshToken;

    public AuthController(Authenticate authenticate, RefreshToken refreshToken) {
        this.authenticate = authenticate;
        this.refreshToken = refreshToken;
    }

    @PostMapping("/login")
    public TokenPair login(@Valid @RequestBody AuthRequest request) {
        return authenticate.handle(request);
    }

    @PostMapping("/refresh")
    public TokenPair refresh(@Valid @RequestBody RefreshRequest request) {
        return refreshToken.handle(request);
    }
}
