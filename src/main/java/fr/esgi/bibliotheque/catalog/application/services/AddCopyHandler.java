package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.CopyRepository;
import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.AddCopy;
import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.TimeProvider;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import fr.esgi.bibliotheque.shared.error.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AddCopyHandler implements AddCopy {

    private static final Logger log = LoggerFactory.getLogger(AddCopyHandler.class);
    private final WorkRepository workRepository;
    private final CopyRepository copyRepository;
    private final DomainIdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public AddCopyHandler(WorkRepository workRepository, CopyRepository copyRepository,
                           DomainIdGenerator idGenerator, TimeProvider timeProvider) {
        this.workRepository = workRepository;
        this.copyRepository = copyRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public CopyId handle(WorkId workId, AddCopyRequest request) {
        var work = workRepository.findById(workId)
            .orElseThrow(() -> new ResourceNotFoundException("Ouvrage introuvable : " + workId.value()));
        if (copyRepository.existsByBarcode(request.barcode())) {
            throw new BusinessException("Un exemplaire avec le code-barres " + request.barcode() + " existe déjà");
        }
        CopyId id = new CopyId(idGenerator.generate());
        Copy copy = Copy.create(id, request.barcode(), work, request.campusId(),
            request.shelf(), request.condition(), timeProvider.now());
        copyRepository.save(copy);
        log.info("Copy created with id={} for workId={}", id.value(), workId.value());
        return id;
    }
}
