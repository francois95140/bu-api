package fr.esgi.bibliotheque.penalty.infrastructure.rest;

import fr.esgi.bibliotheque.penalty.application.models.CreatePenaltyRequest;
import fr.esgi.bibliotheque.penalty.application.usecases.ClearPenalty;
import fr.esgi.bibliotheque.penalty.application.usecases.CreatePenalty;
import fr.esgi.bibliotheque.penalty.application.usecases.SearchPenaltiesByUser;
import fr.esgi.bibliotheque.penalty.domain.PenaltyId;
import fr.esgi.bibliotheque.penalty.infrastructure.rest.dto.PenaltyDto;
import fr.esgi.bibliotheque.users.domain.UserId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/penalties")
public class PenaltyController {

    private final CreatePenalty createPenalty;
    private final ClearPenalty clearPenalty;
    private final SearchPenaltiesByUser searchPenaltiesByUser;

    public PenaltyController(CreatePenalty createPenalty, ClearPenalty clearPenalty,
                              SearchPenaltiesByUser searchPenaltiesByUser) {
        this.createPenalty = createPenalty;
        this.clearPenalty = clearPenalty;
        this.searchPenaltiesByUser = searchPenaltiesByUser;
    }

    @GetMapping
    public List<PenaltyDto> getByUser(@RequestParam String userId) {
        return searchPenaltiesByUser.handle(new UserId(userId))
                .stream().map(PenaltyDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePenaltyRequest request,
                                        UriComponentsBuilder ucb) {
        PenaltyId id = createPenalty.handle(request);
        var uri = ucb.path("/api/penalties/{id}").buildAndExpand(id.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/{id}/clear")
    public ResponseEntity<Void> clear(@PathVariable String id) {
        clearPenalty.handle(new PenaltyId(id));
        return ResponseEntity.ok().build();
    }
}
