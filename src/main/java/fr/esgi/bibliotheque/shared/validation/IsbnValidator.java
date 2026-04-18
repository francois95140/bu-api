package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<ValidIsbn, String> {

    private static final String ISBN_10 = "\\d{9}[\\dX]";
    private static final String ISBN_13 = "\\d{13}";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        String clean = value.replace("-", "").replace(" ", "");
        return clean.matches(ISBN_10) || (clean.matches(ISBN_13) && isIsbn13Valid(clean));
    }

    private boolean isIsbn13Valid(String isbn) {
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = isbn.charAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return check == (isbn.charAt(12) - '0');
    }
}
