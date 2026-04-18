package fr.esgi.bibliotheque.catalog.application.services;

import fr.esgi.bibliotheque.catalog.application.gateways.WorkRepository;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.application.usecases.CreateWork;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import fr.esgi.bibliotheque.shared.DomainIdGenerator;
import fr.esgi.bibliotheque.shared.error.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateWorkHandler implements CreateWork {

    private static final Logger log = LoggerFactory.getLogger(CreateWorkHandler.class);
    private final WorkRepository workRepository;
    private final DomainIdGenerator idGenerator;

    public CreateWorkHandler(WorkRepository workRepository, DomainIdGenerator idGenerator) {
        this.workRepository = workRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public WorkId handle(CreateWorkRequest request) {
        if (workRepository.existsByIsbn(request.isbn())) {
            throw new BusinessException("Un ouvrage avec l'ISBN " + request.isbn() + " existe déjà");
        }
        WorkId id = new WorkId(idGenerator.generate());
        Work work = Work.create(id, request.isbn(), request.title(), request.authors(),
            request.publisher(), request.year(), request.subject(),
            request.language(), request.description());
        workRepository.save(work);
        log.info("Work created with id={}", id.value());
        return id;
    }
}
