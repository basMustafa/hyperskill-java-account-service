package account.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = DateValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDate {
    String message() default "Date must be in MM-YYYY format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
