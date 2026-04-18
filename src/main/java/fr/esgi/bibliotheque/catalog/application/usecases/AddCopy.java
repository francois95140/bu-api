package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.AddCopyRequest;
import fr.esgi.bibliotheque.catalog.domain.CopyId;
import fr.esgi.bibliotheque.catalog.domain.WorkId;
public interface AddCopy { CopyId handle(WorkId workId, AddCopyRequest request); }
