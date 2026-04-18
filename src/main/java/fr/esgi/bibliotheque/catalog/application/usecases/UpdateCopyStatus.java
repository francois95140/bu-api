package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.UpdateCopyStatusRequest;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
public interface UpdateCopyStatus { void handle(CopyId id, UpdateCopyStatusRequest request); }
