package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BarcodeValidator implements ConstraintValidator<ValidBarcode, String> {

    private static final String BARCODE_PATTERN = "[A-Z0-9]{8,20}";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        return value.matches(BARCODE_PATTERN);
    }
}
