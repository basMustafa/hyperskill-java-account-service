package account.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChangeAccessDTO {

    private final String user;
    private final String operation;
}
