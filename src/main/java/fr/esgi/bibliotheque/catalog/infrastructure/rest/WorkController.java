package fr.esgi.bibliotheque.catalog.infrastructure.rest;

import fr.esgi.bibliotheque.catalog.application.models.*;
import fr.esgi.bibliotheque.catalog.application.usecases.*;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkDetailDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.dto.WorkSummaryDto;
import fr.esgi.bibliotheque.catalog.infrastructure.rest.mapper.WorkMapper;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/works")
public class WorkController {

    private final CreateWork createWork;
    private final UpdateWork updateWork;
    private final SearchWorkByIdWithCopies searchWorkByIdWithCopies;
    private final SearchWorks searchWorks;
    private final DeleteWork deleteWork;
    private final AddCopy addCopy;
    private final WorkMapper mapper;

    public WorkController(CreateWork createWork, UpdateWork updateWork,
                           SearchWorkByIdWithCopies searchWorkByIdWithCopies,
                           SearchWorks searchWorks, DeleteWork deleteWork,
                           AddCopy addCopy, WorkMapper mapper) {
        this.createWork = createWork;
        this.updateWork = updateWork;
        this.searchWorkByIdWithCopies = searchWorkByIdWithCopies;
        this.searchWorks = searchWorks;
        this.deleteWork = deleteWork;
        this.addCopy = addCopy;
        this.mapper = mapper;
    }

    @GetMapping
    public List<WorkSummaryDto> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String authors,
            @RequestParam(required = false) String subject) {
        return mapper.toSummaryDtoList(searchWorks.handle(new WorkFilters(title, isbn, authors, subject)));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateWorkRequest request,
                                        UriComponentsBuilder ucb) {
        WorkId id = createWork.handle(request);
        var uri = ucb.path("/api/works/{id}").buildAndExpand(id.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/{id}")
    public WorkDetailDto getById(@PathVariable String id) {
        return searchWorkByIdWithCopies.handle(new WorkId(id))
            .map(mapper::toDetailDto)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable String id,
                                        @Valid @RequestBody UpdateWorkRequest request) {
        updateWork.handle(new WorkId(id), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteWork.handle(new WorkId(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{workId}/copies")
    public ResponseEntity<Void> addCopy(@PathVariable String workId,
                                         @Valid @RequestBody AddCopyRequest request,
                                         UriComponentsBuilder ucb) {
        CopyId copyId = addCopy.handle(new WorkId(workId), request);
        var uri = ucb.path("/api/copies/{id}").buildAndExpand(copyId.value()).toUri();
        return ResponseEntity.created(uri).build();
    }

}
