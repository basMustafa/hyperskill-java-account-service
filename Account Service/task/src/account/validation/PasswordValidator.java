package account.validation;

import account.exception.BreachedPasswordException;
import account.exception.PasswordLengthException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private final List<String> breachedPasswords = List.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else if (breachedPasswords.contains(password)) {
            throw new BreachedPasswordException();
        } else if (password.length() < 12) {
            throw new PasswordLengthException();
        }
        return true;
    }
}
