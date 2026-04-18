package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BarcodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBarcode {
    String message() default "{ValidBarcode.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
