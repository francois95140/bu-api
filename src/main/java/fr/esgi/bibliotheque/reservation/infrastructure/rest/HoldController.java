package fr.esgi.bibliotheque.reservation.infrastructure.rest;

import fr.esgi.bibliotheque.reservation.application.models.RequestHoldRequest;
import fr.esgi.bibliotheque.reservation.application.usecases.CancelHold;
import fr.esgi.bibliotheque.reservation.application.usecases.PickupHold;
import fr.esgi.bibliotheque.reservation.application.usecases.RequestHold;
import fr.esgi.bibliotheque.reservation.domain.HoldId;
import fr.esgi.bibliotheque.reservation.infrastructure.rest.dto.HoldDto;
import fr.esgi.bibliotheque.reservation.infrastructure.rest.mapper.HoldMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/holds")
public class HoldController {

    private final RequestHold requestHold;
    private final CancelHold cancelHold;
    private final PickupHold pickupHold;
    private final HoldMapper mapper;

    public HoldController(RequestHold requestHold, CancelHold cancelHold,
                           PickupHold pickupHold, HoldMapper mapper) {
        this.requestHold = requestHold;
        this.cancelHold = cancelHold;
        this.pickupHold = pickupHold;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<HoldDto> request(@Valid @RequestBody RequestHoldRequest request,
                                            UriComponentsBuilder ucb) {
        var hold = requestHold.handle(request);
        var uri = ucb.path("/api/holds/{id}").buildAndExpand(hold.getId().value()).toUri();
        return ResponseEntity.created(uri).body(mapper.toDto(hold));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable String id) {
        cancelHold.handle(new HoldId(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/pickup")
    public ResponseEntity<Void> pickup(@PathVariable String id) {
        pickupHold.handle(new HoldId(id));
        return ResponseEntity.ok().build();
    }
}
