package fr.esgi.bibliotheque.users.infrastructure.rest;

import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import fr.esgi.bibliotheque.users.application.models.RegisterUserRequest;
import fr.esgi.bibliotheque.users.application.models.UpdateUserRequest;
import fr.esgi.bibliotheque.users.application.models.UserFilters;
import fr.esgi.bibliotheque.users.application.usecases.*;
import fr.esgi.bibliotheque.users.domain.UserCategory;
import fr.esgi.bibliotheque.users.domain.UserId;
import fr.esgi.bibliotheque.users.domain.UserStatus;
import fr.esgi.bibliotheque.users.infrastructure.rest.dto.UserDetailDto;
import fr.esgi.bibliotheque.users.infrastructure.rest.dto.UserSummaryDto;
import fr.esgi.bibliotheque.users.infrastructure.rest.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final RegisterUser registerUser;
    private final UpdateUser updateUser;
    private final SearchUserById searchUserById;
    private final SearchUsers searchUsers;
    private final BlockUser blockUser;
    private final UnblockUser unblockUser;
    private final UserMapper mapper;

    public UserController(RegisterUser registerUser, UpdateUser updateUser,
                           SearchUserById searchUserById, SearchUsers searchUsers,
                           BlockUser blockUser, UnblockUser unblockUser,
                           UserMapper mapper) {
        this.registerUser = registerUser;
        this.updateUser = updateUser;
        this.searchUserById = searchUserById;
        this.searchUsers = searchUsers;
        this.blockUser = blockUser;
        this.unblockUser = unblockUser;
        this.mapper = mapper;
    }

    @GetMapping
    public List<UserSummaryDto> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UserCategory category,
            @RequestParam(required = false) UserStatus status) {
        return mapper.toSummaryDtoList(searchUsers.handle(new UserFilters(name, email, category, status)));
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterUserRequest request,
                                          UriComponentsBuilder ucb) {
        UserId id = registerUser.handle(request);
        var uri = ucb.path("/api/users/{id}").buildAndExpand(id.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public UserDetailDto getById(@PathVariable String id) {
        return searchUserById.handle(new UserId(id))
                .map(mapper::toDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id,
                                        @Valid @RequestBody UpdateUserRequest request) {
        updateUser.handle(new UserId(id), request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> block(@PathVariable String id) {
        blockUser.handle(new UserId(id));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(@PathVariable String id) {
        unblockUser.handle(new UserId(id));
        return ResponseEntity.ok().build();
    }
}
