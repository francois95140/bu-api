package fr.esgi.bibliotheque.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIsbn {
    String message() default "{ValidIsbn.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
