package account.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateValidator implements ConstraintValidator<ValidDate, String> {
    @Override
    public boolean isValid(String string, ConstraintValidatorContext context) {
        if (string == null)
            return true;
        return (string
                .strip()
                .replaceAll("\"", "")
                .strip().matches("^(1[0-2]|0?[0-9])-20[0-9]{2}$"));
    }
}
