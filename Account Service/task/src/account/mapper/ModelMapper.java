package account.mapper;

import account.dto.payment.PaymentDTO;
import account.dto.payment.PaymentResponseDTO;
import account.dto.user.UserDTO;
import account.model.payment.Payment;
import account.model.user.Role;
import account.model.user.User;
import account.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class ModelMapper {

    private final UserService userService;

    @Autowired
    public ModelMapper(UserService userService) {
        this.userService = userService;
    }

    public User mapToEntity(UserDTO dto) {
        return User.builder()
                .name(dto.getName())
                .lastname(dto.getLastname())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .isAccountNonLocked(true)
                .build();
    }

    public UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(Role::withPrefix)
                        .sorted(String::compareTo)
                        .collect(Collectors.toList()))
                .build();
    }

    public Payment mapToEntity(PaymentDTO dto) {
        return Payment.builder()
                .email(dto.getEmail())
                .period(stringToDate(dto.getPeriod()))
                .salary(dto.getSalary())
                .user(userService.findUserByEmail(dto.getEmail()))
                .build();
    }

    public PaymentResponseDTO mapToDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .name(payment.getUser().getName())
                .lastname(payment.getUser().getLastname())
                .period(dateToString(payment.getPeriod()))
                .salary(formatSalary(payment.getSalary()))
                .build();
    }

    private LocalDate stringToDate(String period) {
        return YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy")).atDay(1);
    }

    private String dateToString(LocalDate period) {
        return period.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));
    }

    private String formatSalary(Long salary) {
        return String.format("%d dollar(s) %d cent(s)",
                salary / 100,
                salary % 100);
    }
}
