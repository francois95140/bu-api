package fr.esgi.bibliotheque.catalog.infrastructure.rest;

import fr.esgi.bibliotheque.catalog.application.models.UpdateCopyStatusRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.UpdateCopyStatus;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/copies")
public class CopyController {

    private final UpdateCopyStatus updateCopyStatus;

    public CopyController(UpdateCopyStatus updateCopyStatus) {
        this.updateCopyStatus = updateCopyStatus;
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable String id,
                                              @Valid @RequestBody UpdateCopyStatusRequest request) {
        updateCopyStatus.handle(new CopyId(id), request);
        return ResponseEntity.ok().build();
    }
}
