package fr.esgi.bibliotheque.catalog.application.models;

import fr.esgi.bibliotheque.shared.validation.ValidBarcode;
import jakarta.validation.constraints.*;

public record AddCopyRequest(
    @NotBlank(message = "Le code-barres est obligatoire") @ValidBarcode String barcode,
    @NotBlank(message = "Le campus est obligatoire") String campusId,
    @Size(max = 50) String shelf,
    @Size(max = 100) String condition
) {}
