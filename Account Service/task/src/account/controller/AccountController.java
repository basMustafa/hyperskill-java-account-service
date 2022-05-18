package account.controller;

import account.dto.payment.PaymentDTO;
import account.dto.user.StatusResponseDTO;
import account.mapper.ModelMapper;
import account.model.payment.Payment;
import account.model.user.User;
import account.service.PaymentService;
import account.service.UserService;
import account.validation.ValidDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
public class AccountController {

    private final UserService userService;
    private final PaymentService paymentService;
    private final ModelMapper modelMapper;

    @Autowired
    public AccountController(UserService userService, PaymentService paymentService, ModelMapper modelMapper) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/api/acct/payments")
    public StatusResponseDTO uploadPayrolls(@RequestBody
                                     @NotEmpty(message = "Input salary list can't be empty.")
                                                 List<@Valid PaymentDTO> payrollDTO) {

        List<Payment> payroll = payrollDTO
                .stream()
                .map(modelMapper::mapToEntity)
                .collect(Collectors.toList());

        paymentService.savePayments(payroll);

        return new StatusResponseDTO("Added successfully!");
    }

    @PutMapping("/api/acct/payments")
    public StatusResponseDTO updatePayment(@Valid @RequestBody PaymentDTO dto) {
        Payment payment = modelMapper.mapToEntity(dto);
        paymentService.updatePayment(payment);

        return new StatusResponseDTO("Updated successfully!");
    }

    @GetMapping("/api/empl/payment")
    public ResponseEntity<?> getPayment(@ValidDate @RequestParam(required = false) String period,
                                        @AuthenticationPrincipal UserDetails userDetails) {

        if (period == null) {
            User user = userService.findUserByEmail(userDetails.getUsername());
            return new ResponseEntity<>(
                    user.getPayments()
                            .stream()
                            .sorted(Comparator.comparing(Payment::getPeriod).reversed())
                            .map(modelMapper::mapToDTO)
                            .collect(Collectors.toList()),
                    HttpStatus.OK);
        } else {
            Payment payment = paymentService.getPayment(userDetails.getUsername(), period);
            return new ResponseEntity<>(modelMapper.mapToDTO(payment), HttpStatus.OK);
        }
    }
}
