package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.UpdateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface UpdateWork { void handle(WorkId id, UpdateWorkRequest request); }
