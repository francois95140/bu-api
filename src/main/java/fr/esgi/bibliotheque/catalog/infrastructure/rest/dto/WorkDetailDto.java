package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Work;
import java.util.List;

public record WorkDetailDto(
    String id,
    String isbn,
    String title,
    String authors,
    String publisher,
    Integer year,
    String subject,
    String language,
    String description,
    List<CopyDto> copies
) {
    public static WorkDetailDto from(Work work) {
        return new WorkDetailDto(
            work.getId().value(),
            work.getIsbn(),
            work.getTitle(),
            work.getAuthors(),
            work.getPublisher(),
            work.getYear(),
            work.getSubject(),
            work.getLanguage(),
            work.getDescription(),
            work.getCopies().stream().map(CopyDto::from).toList()
        );
    }
}
