package account.dto.payment;

import lombok.*;

@Getter
@Builder
public class PaymentResponseDTO {

    private final String name;
    private final String lastname;
    private final String period;
    private final String salary;
}
