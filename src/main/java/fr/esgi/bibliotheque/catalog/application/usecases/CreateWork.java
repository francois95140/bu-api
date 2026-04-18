package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.CreateWorkRequest;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface CreateWork { WorkId handle(CreateWorkRequest request); }
