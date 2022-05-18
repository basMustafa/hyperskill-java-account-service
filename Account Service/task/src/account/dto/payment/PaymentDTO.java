package account.dto.payment;

import account.validation.ValidDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;

@Getter
@RequiredArgsConstructor
public class PaymentDTO {

    @Email(regexp = "\\w+(@acme.com)$")
    @JsonProperty("employee")
    private final String email;
    @ValidDate
    private final String period;
    @Min(value = 0)
    private final Long salary;
}
