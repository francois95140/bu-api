package fr.esgi.bibliotheque.catalog.infrastructure.rest.dto;

import fr.esgi.bibliotheque.catalog.domain.Copy;
import fr.esgi.bibliotheque.catalog.domain.CopyStatus;

public record CopyDto(
    String id,
    String barcode,
    CopyStatus status,
    String campusId,
    String shelf,
    String condition
) {
    public static CopyDto from(Copy copy) {
        return new CopyDto(
            copy.getId().value(),
            copy.getBarcode(),
            copy.getStatus(),
            copy.getCampusId(),
            copy.getShelf(),
            copy.getCondition()
        );
    }
}
