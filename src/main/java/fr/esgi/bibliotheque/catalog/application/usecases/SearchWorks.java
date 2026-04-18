package fr.esgi.bibliotheque.catalog.application.usecases;
import fr.esgi.bibliotheque.catalog.application.models.WorkFilters;
import fr.esgi.bibliotheque.catalog.domain.Work;
import java.util.List;
public interface SearchWorks { List<Work> handle(WorkFilters filters); }
