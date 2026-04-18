package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.domain.Work;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
import java.util.Optional;
public interface SearchWorkById { Optional<Work> handle(WorkId id); }
