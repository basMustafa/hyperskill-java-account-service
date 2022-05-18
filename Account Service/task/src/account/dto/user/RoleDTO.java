package account.dto.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RoleDTO {

    private final String user;
    private final String role;
    private final String operation;
}
