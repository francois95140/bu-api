package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Work;

public record WorkSummaryDto(
    String id,
    String isbn,
    String title,
    String authors,
    String publisher,
    Integer year,
    String subject,
    String language
) {
    public static WorkSummaryDto from(Work work) {
        return new WorkSummaryDto(
            work.getId().value(),
            work.getIsbn(),
            work.getTitle(),
            work.getAuthors(),
            work.getPublisher(),
            work.getYear(),
            work.getSubject(),
            work.getLanguage()
        );
    }
}
